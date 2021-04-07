package gov.usds.vaccineschedule.api.repositories;

import gov.usds.vaccineschedule.api.db.models.AddressElement_;
import gov.usds.vaccineschedule.api.db.models.LocationEntity;
import gov.usds.vaccineschedule.api.db.models.LocationEntity_;
import gov.usds.vaccineschedule.api.db.models.LocationIdentifier;
import gov.usds.vaccineschedule.api.db.models.LocationIdentifier_;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CollectionJoin;
import java.util.List;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<LocationEntity, UUID>, JpaSpecificationExecutor<LocationEntity> {

    @Query(
            "from LocationEntity l where distance(l.coordinates, :location) < :distance"
    )
    List<LocationEntity> locationsWithinDistance(Point location, double distance, Pageable page);

    @Query(
            "select count(*) from LocationEntity l where distance(l.coordinates, :location) < :distance"
    )
    long countLocationsWithinDistance(Point location, double distance);

    static Specification<LocationEntity> hasIdentifier(String system, String value) {
        return (root, cq, cb) -> {
            final CollectionJoin<LocationEntity, LocationIdentifier> idJoin = root.join(LocationEntity_.identifiers);

            return cb.and(
                    cb.equal(idJoin.get(LocationIdentifier_.system), system),
                    cb.equal(idJoin.get(LocationIdentifier_.value), value));
        };
    }

    static Specification<LocationEntity> inCity(String city) {
        return (root, cq, cb) -> cb.equal(root.get(LocationEntity_.address).get(AddressElement_.city), city);
    }

    static Specification<LocationEntity> inState(String state) {
        return (root, cq, cb) -> cb.equal(root.get(LocationEntity_.address).get(AddressElement_.state), state);
    }

    static Specification<LocationEntity> inPostalCode(String postalCode) {
        return (root, cq, cb) -> cb.equal(root.get(LocationEntity_.address).get(AddressElement_.postalCode), postalCode);
    }
}
