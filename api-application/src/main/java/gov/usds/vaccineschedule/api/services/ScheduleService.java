package gov.usds.vaccineschedule.api.services;

import gov.usds.vaccineschedule.api.db.models.LocationEntity;
import gov.usds.vaccineschedule.api.db.models.ScheduleEntity;
import gov.usds.vaccineschedule.api.repositories.LocationRepository;
import gov.usds.vaccineschedule.api.repositories.ScheduleRepository;
import org.hl7.fhir.r4.model.Schedule;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static gov.usds.vaccineschedule.api.db.models.Constants.ORIGINAL_ID_SYSTEM;
import static gov.usds.vaccineschedule.api.repositories.LocationRepository.hasIdentifier;
import static gov.usds.vaccineschedule.api.repositories.ScheduleRepository.byLocation;


/**
 * Created by nickrobison on 3/26/21
 */
@Service
@Transactional(readOnly = true)
public class ScheduleService {

    private final ScheduleRepository repo;
    private final LocationRepository lRepo;

    public ScheduleService(ScheduleRepository repo, LocationRepository lRepo) {
        this.repo = repo;
        this.lRepo = lRepo;
    }

    public Collection<Schedule> getSchedule() {
        return StreamSupport
                .stream(this.repo.findAll().spliterator(), false)
                .map(ScheduleEntity::toFHIR)
                .collect(Collectors.toList());
    }

    public Collection<Schedule> getSchedulesForLocation(String reference) {
        final UUID locationId = UUID.fromString(reference);
        return StreamSupport
                .stream(this.repo.findAll(byLocation(locationId)).spliterator(), false)
                .map(ScheduleEntity::toFHIR)
                .collect(Collectors.toList());
    }

    public Collection<Schedule> addSchedules(Collection<Schedule> resources) {
        return resources
                .stream()
                .map(this::addSchedule)
                .map(ScheduleEntity::toFHIR)
                .collect(Collectors.toList());
    }

    @Transactional
    public ScheduleEntity addSchedule(Schedule resource) {

        // Figure out which location we need to search for
        final String reference = resource.getActor().get(0).getReference();
        final List<LocationEntity> locations = lRepo.findAll(hasIdentifier(ORIGINAL_ID_SYSTEM, reference));
        if (locations.isEmpty()) {
            throw new IllegalStateException("Cannot add to missing location");
        }
        // Otherwise, grab the first one
        final ScheduleEntity entity = ScheduleEntity.fromFHIR(locations.get(0), resource);
        return repo.save(entity);


    }


}
