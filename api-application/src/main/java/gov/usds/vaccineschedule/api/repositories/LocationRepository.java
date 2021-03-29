package gov.usds.vaccineschedule.api.repositories;

import gov.usds.vaccineschedule.api.db.models.LocationEntity;
import gov.usds.vaccineschedule.api.db.models.LocationEntity_;
import gov.usds.vaccineschedule.api.db.models.LocationIdentifier;
import gov.usds.vaccineschedule.api.db.models.LocationIdentifier_;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CollectionJoin;
import java.util.List;
import java.util.UUID;

@Repository
public interface LocationRepository extends CrudRepository<LocationEntity, UUID> {
    List<LocationEntity> findAll(Specification<LocationEntity> searchSpec);

    static Specification<LocationEntity> hasIdentifier(String system, String value) {
        return (root, cq, cb) -> {
            final CollectionJoin<LocationEntity, LocationIdentifier> idJoin = root.join(LocationEntity_.identifiers);

            return cb.and(
                    cb.equal(idJoin.get(LocationIdentifier_.system), system),
                    cb.equal(idJoin.get(LocationIdentifier_.value), value));
        };
    }
}
