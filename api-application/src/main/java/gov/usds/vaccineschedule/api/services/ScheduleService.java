package gov.usds.vaccineschedule.api.services;

import gov.usds.vaccineschedule.api.db.models.LocationEntity;
import gov.usds.vaccineschedule.api.db.models.LocationEntity_;
import gov.usds.vaccineschedule.api.db.models.LocationIdentifier;
import gov.usds.vaccineschedule.api.db.models.LocationIdentifier_;
import gov.usds.vaccineschedule.api.db.models.ScheduleEntity;
import gov.usds.vaccineschedule.api.repositories.LocationRepository;
import gov.usds.vaccineschedule.api.repositories.ScheduleRepository;
import org.hl7.fhir.r4.model.Schedule;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CollectionJoin;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static gov.usds.vaccineschedule.api.db.models.Constants.ORIGINAL_ID_SYSTEM;


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

    @Transactional(readOnly = false)
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

    private static Specification<LocationEntity> hasIdentifier(String system, String value) {
        return (root, cq, cb) -> {
            final CollectionJoin<LocationEntity, LocationIdentifier> idJoin = root.join(LocationEntity_.identifiers);

            return cb.and(
                    cb.equal(idJoin.get(LocationIdentifier_.system), system),
                    cb.equal(idJoin.get(LocationIdentifier_.value), value));
        };
    }
}
