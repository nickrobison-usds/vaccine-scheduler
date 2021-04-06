package gov.usds.vaccineschedule.publisher.respositories;

import org.hl7.fhir.r4.model.Schedule;

import java.util.List;

public interface ScheduleRepository {

    List<Schedule> getAll();
}
