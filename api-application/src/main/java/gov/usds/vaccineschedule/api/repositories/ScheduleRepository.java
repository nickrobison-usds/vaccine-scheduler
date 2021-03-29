package gov.usds.vaccineschedule.api.repositories;

import gov.usds.vaccineschedule.api.db.models.ScheduleEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ScheduleRepository extends CrudRepository<ScheduleEntity, UUID> {
}
