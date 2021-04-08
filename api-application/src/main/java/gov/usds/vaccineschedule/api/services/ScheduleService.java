package gov.usds.vaccineschedule.api.services;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationFailureException;
import ca.uhn.fhir.validation.ValidationOptions;
import ca.uhn.fhir.validation.ValidationResult;
import gov.usds.vaccineschedule.api.db.models.LocationEntity;
import gov.usds.vaccineschedule.api.db.models.ScheduleEntity;
import gov.usds.vaccineschedule.api.repositories.LocationRepository;
import gov.usds.vaccineschedule.api.repositories.ScheduleRepository;
import gov.usds.vaccineschedule.common.Constants;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Schedule;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static gov.usds.vaccineschedule.api.repositories.ScheduleRepository.byLocation;
import static gov.usds.vaccineschedule.api.repositories.ScheduleRepository.hasIdentifier;
import static gov.usds.vaccineschedule.common.Constants.ORIGINAL_ID_SYSTEM;


/**
 * Created by nickrobison on 3/26/21
 */
@Service
@Transactional(readOnly = true)
public class ScheduleService {

    private final ScheduleRepository repo;
    private final LocationRepository lRepo;
    private final FhirContext ctx;
    private final FhirValidator validator;

    public ScheduleService(ScheduleRepository repo, LocationRepository lRepo, FhirContext ctx, FhirValidator validator) {
        this.repo = repo;
        this.lRepo = lRepo;
        this.ctx = ctx;
        this.validator = validator;
    }

    public List<Schedule> getAllSchedules(Pageable pageable) {
        return StreamSupport
                .stream(this.repo.findAll(pageable).spliterator(), false)
                .map(ScheduleEntity::toFHIR)
                .collect(Collectors.toList());
    }

    public long countAllSchedules() {
        return this.repo.count();
    }

    public Optional<Schedule> getSchedule(IIdType id) {
        return this.repo.findById(UUID.fromString(id.getIdPart())).map(ScheduleEntity::toFHIR);
    }

    public long countScheduleWithIdentifier(TokenParam param) {
        return this.repo.count(hasIdentifier(param.getSystem(), param.getValue()));
    }

    public List<Schedule> getScheduleWithIdentifier(TokenParam param, Pageable pageable) {
        return StreamSupport.stream(this.repo.findAll(hasIdentifier(param.getSystem(), param.getValue()), pageable).spliterator(), false)
                .map(ScheduleEntity::toFHIR)
                .collect(Collectors.toList());
    }

    public long countSchedulesForLocation(String reference) {
        final UUID locationId = UUID.fromString(reference);
        return this.repo.count(byLocation(locationId));
    }

    public List<Schedule> getSchedulesForLocation(String reference, Pageable pageable) {
        final UUID locationId = UUID.fromString(reference);
        return StreamSupport
                .stream(this.repo.findAll(byLocation(locationId), pageable).spliterator(), false)
                .map(ScheduleEntity::toFHIR)
                .collect(Collectors.toList());
    }

    public Collection<Schedule> addSchedules(Collection<Schedule> resources) {
        return resources
                .stream()
                .map(this::addSchedule)
                .collect(Collectors.toList());
    }

    @Transactional
    public Schedule addSchedule(Schedule resource) {
        this.validateSchedule(resource);
        // Figure out which location we need to search for
        final String reference = resource.getActor().get(0).getReference();
        final List<LocationEntity> locations = lRepo.findAll(LocationRepository.hasIdentifier(ORIGINAL_ID_SYSTEM, reference));
        if (locations.isEmpty()) {
            throw new IllegalStateException("Cannot add to missing location");
        }
        final ScheduleEntity entity = ScheduleEntity.fromFHIR(locations.get(0), resource);

        final Optional<ScheduleEntity> maybeExists = this.repo.findOne(hasIdentifier(ORIGINAL_ID_SYSTEM, resource.getId()));
        if (maybeExists.isPresent()) {
            final ScheduleEntity exists = maybeExists.get();
            exists.merge(entity);
            return repo.save(exists).toFHIR();
        } else {
            return repo.save(entity).toFHIR();
        }
    }

    private void validateSchedule(Schedule schedule) {
        final ValidationOptions options = new ValidationOptions();
        options.addProfile(Constants.SCHEDULE_PROFILE);

        final ValidationResult result = this.validator.validateWithResult(schedule, options);
        if (!result.isSuccessful()) {
            throw new ValidationFailureException(this.ctx, result.toOperationOutcome());
        }
    }
}
