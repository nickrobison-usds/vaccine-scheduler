package cov.usds.vaccineshedule.publisher.respositories;

import org.hl7.fhir.r4.model.Location;

import java.util.List;

public interface LocationRepository {

    List<Location> getAll();
}
