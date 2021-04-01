package gov.usds.vaccineschedule.api.repositories;

import gov.usds.vaccineschedule.api.db.models.LocationEntity;
import gov.usds.vaccineschedule.api.db.models.LocationEntity_;
import gov.usds.vaccineschedule.api.db.models.ScheduleEntity;
import gov.usds.vaccineschedule.api.db.models.ScheduleEntity_;
import gov.usds.vaccineschedule.api.db.models.SlotEntity;
import gov.usds.vaccineschedule.api.db.models.SlotEntity_;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.Join;
import java.util.List;
import java.util.UUID;

@Repository
public interface SlotRepository extends CrudRepository<SlotEntity, UUID> {

    List<SlotEntity> findAll(Specification<SlotEntity> searchSpec);

    static Specification<SlotEntity> forLocation(UUID locationId) {
        return (root, cq, cb) -> {
            final Join<SlotEntity, ScheduleEntity> scheduleJoin = root.join(SlotEntity_.schedule);

            final Join<ScheduleEntity, LocationEntity> locationJoin = scheduleJoin.join(ScheduleEntity_.location);

            return cb.equal(locationJoin.get(LocationEntity_.internalId), locationId);
        };
    }
}
