package gov.usds.vaccineschedule.api.services;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationFailureException;
import ca.uhn.fhir.validation.ValidationOptions;
import ca.uhn.fhir.validation.ValidationResult;
import gov.usds.vaccineschedule.api.db.models.ScheduleEntity;
import gov.usds.vaccineschedule.api.db.models.SlotEntity;
import gov.usds.vaccineschedule.api.exceptions.MissingUpstreamResource;
import gov.usds.vaccineschedule.api.repositories.ScheduleRepository;
import gov.usds.vaccineschedule.api.repositories.SlotRepository;
import gov.usds.vaccineschedule.common.Constants;
import gov.usds.vaccineschedule.common.models.VaccineSlot;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Schedule;
import org.hl7.fhir.r4.model.Slot;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static gov.usds.vaccineschedule.api.repositories.SlotRepository.forLocation;
import static gov.usds.vaccineschedule.api.repositories.SlotRepository.forLocationAndTime;
import static gov.usds.vaccineschedule.api.repositories.SlotRepository.withIdentifier;
import static gov.usds.vaccineschedule.common.Constants.ORIGINAL_ID_SYSTEM;

/**
 * Created by nickrobison on 3/29/21
 */
@Service
@Transactional(readOnly = true)
public class SlotService {

    private final ScheduleRepository scheduleRepository;
    private final SlotRepository repo;
    private final FhirContext ctx;
    private final FhirValidator validator;


    public SlotService(ScheduleRepository scheduleRepository, SlotRepository repo, FhirContext ctx, FhirValidator validator) {
        this.scheduleRepository = scheduleRepository;
        this.repo = repo;
        this.ctx = ctx;
        this.validator = validator;
    }


    public long countSlotsWithId(TokenParam identifier) {
        return this.repo.count(withIdentifier(identifier.getSystem(), identifier.getValue()));
    }

    public List<Slot> findSlotsWithId(TokenParam identifier, Pageable pageable, boolean includeSchedule) {
        return this.repo.findAll(withIdentifier(identifier.getSystem(), identifier.getValue()), pageable)
                .stream().map(e -> e.toFHIR(includeSchedule))
                .collect(Collectors.toList());
    }

    public List<Slot> getSlots(Pageable pageable, boolean includeSchedule) {
        return StreamSupport
                .stream(this.repo.findAll(pageable).spliterator(), false)
                .map(e -> e.toFHIR(includeSchedule))
                .collect(Collectors.toList());
    }

    public long countSlots() {
        return this.repo.count();
    }

    public long countSlotsForLocations(List<ReferenceParam> idParam, @Nullable DateRangeParam dateParam) {
        final Specification<SlotEntity> query = buildLocationSearchQuery(idParam, dateParam);
        return this.repo.count(query);
    }

    public List<Slot> getSlotsForLocations(List<ReferenceParam> idParam, @Nullable DateRangeParam dateParam, Pageable pageable, boolean includeSchedule) {
        final Specification<SlotEntity> searchParams = buildLocationSearchQuery(idParam, dateParam);

        return this.repo.findAll(searchParams, pageable)
                .stream()
                .map(e -> e.toFHIR(includeSchedule))
                .collect(Collectors.toList());
    }

    @Transactional
    public Collection<Slot> addSlots(Collection<VaccineSlot> resources) {
        return resources
                .stream().map(this::addSlot)
                .collect(Collectors.toList());
    }

    @Transactional
    public Slot addSlot(VaccineSlot resource) {
        this.validateSlot(resource);
        final String scheduleRef = resource.getSchedule().getReference();

        final List<ScheduleEntity> schedule = new ArrayList<>(scheduleRepository.findAll(ScheduleRepository.hasIdentifier(ORIGINAL_ID_SYSTEM, scheduleRef)));
        if (schedule.isEmpty()) {
            throw new MissingUpstreamResource(Schedule.class, new Identifier().setSystem(ORIGINAL_ID_SYSTEM).setValue(scheduleRef));
        }

        final SlotEntity entity = SlotEntity.fromFHIR(schedule.get(0), resource);

        final Optional<SlotEntity> maybeExists = this.repo.findOne(withIdentifier(ORIGINAL_ID_SYSTEM, resource.getId()));
        if (maybeExists.isPresent()) {
            // Merge
            final SlotEntity existing = maybeExists.get();
            existing.merge(entity);
            return repo.save(existing).toFHIR();
        } else {
            return repo.save(entity).toFHIR();
        }
    }

    private static Specification<SlotEntity> buildLocationSearchQuery(List<ReferenceParam> idParams, DateRangeParam dateParam) {
        final List<UUID> id = idParams.stream().map(param -> UUID.fromString(param.getIdPart())).collect(Collectors.toList());

        final Specification<SlotEntity> searchParams;
        if (dateParam == null) {
            searchParams = forLocation(id);
        } else {
            searchParams = forLocationAndTime(id, dateParam);
        }
        return searchParams;
    }

    private void validateSlot(VaccineSlot slot) {
        final ValidationOptions options = new ValidationOptions();
        options.addProfile(Constants.SLOT_PROFILE);

        final ValidationResult result = this.validator.validateWithResult(slot, options);
        if (!result.isSuccessful()) {
            throw new ValidationFailureException(this.ctx, result.toOperationOutcome());
        }
    }
}
