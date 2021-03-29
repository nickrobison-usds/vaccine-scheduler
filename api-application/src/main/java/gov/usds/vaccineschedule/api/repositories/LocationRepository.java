package gov.usds.vaccineschedule.api.repositories;

import gov.usds.vaccineschedule.api.db.models.LocationEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LocationRepository extends CrudRepository<LocationEntity, UUID> {
    List<LocationEntity> findAll(Specification<LocationEntity> searchSpec);
}
