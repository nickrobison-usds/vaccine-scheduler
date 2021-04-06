package gov.usds.vaccineschedule.publisher.respositories;

import org.hl7.fhir.r4.model.Location;

import java.util.List;

public interface LocationRepository {

    List<Location> getAll();
}
