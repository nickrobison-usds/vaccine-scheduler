package gov.usds.vaccineschedule.api.repositories;

import gov.usds.vaccineschedule.api.db.models.SlotEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SlotRepository extends CrudRepository<SlotEntity, UUID> {
}
