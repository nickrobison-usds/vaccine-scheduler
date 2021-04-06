package gov.usds.vaccineschedule.api.repositories;

import gov.usds.vaccineschedule.api.db.models.LocationEntity;
import gov.usds.vaccineschedule.api.db.models.LocationEntity_;
import gov.usds.vaccineschedule.api.db.models.ScheduleEntity;
import gov.usds.vaccineschedule.api.db.models.ScheduleEntity_;
import gov.usds.vaccineschedule.api.db.models.ScheduleIdentifier;
import gov.usds.vaccineschedule.api.db.models.ScheduleIdentifier_;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.Join;
import java.util.UUID;

@Repository
public interface ScheduleRepository extends JpaRepository<ScheduleEntity, UUID>, JpaSpecificationExecutor<ScheduleEntity> {
    static Specification<ScheduleEntity> byLocation(UUID locationId) {
        return (root, cq, cb) -> {
            final Join<ScheduleEntity, LocationEntity> join = root.join(ScheduleEntity_.location);

            return cb.equal(join.get(LocationEntity_.internalId), locationId);
        };
    }

    static Specification<ScheduleEntity> hasIdentifier(String system, String value) {
        return (root, cq, cb) -> {
            final CollectionJoin<ScheduleEntity, ScheduleIdentifier> idJoin = root.join(ScheduleEntity_.identifiers);

            return cb.and(
                    cb.equal(idJoin.get(ScheduleIdentifier_.system), system),
                    cb.equal(idJoin.get(ScheduleIdentifier_.value), value));
        };
    }
}
