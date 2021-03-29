package gov.usds.vaccineschedule.api.repositories;

import gov.usds.vaccineschedule.api.db.models.ScheduleEntity;
import gov.usds.vaccineschedule.api.db.models.ScheduleEntity_;
import gov.usds.vaccineschedule.api.db.models.ScheduleIdentifier;
import gov.usds.vaccineschedule.api.db.models.ScheduleIdentifier_;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CollectionJoin;
import java.util.List;
import java.util.UUID;

@Repository
public interface ScheduleRepository extends CrudRepository<ScheduleEntity, UUID> {
    List<ScheduleEntity> findAll(Specification<ScheduleEntity> searchSpec);

    static Specification<ScheduleEntity> hasIdentifier(String system, String value) {
        return (root, cq, cb) -> {
            final CollectionJoin<ScheduleEntity, ScheduleIdentifier> idJoin = root.join(ScheduleEntity_.identifiers);

            return cb.and(
                    cb.equal(idJoin.get(ScheduleIdentifier_.system), system),
                    cb.equal(idJoin.get(ScheduleIdentifier_.value), value));
        };
    }
}
