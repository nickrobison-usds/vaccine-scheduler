package gov.usds.vaccineschedule.publisher.respositories;

import ca.uhn.fhir.context.FhirContext;
import gov.usds.vaccineschedule.common.helpers.NDJSONToFHIR;
import org.hl7.fhir.r4.model.Location;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by nickrobison on 3/25/21
 */
@Service
public class MockLocationRepository implements LocationRepository {

    private static final List<Location> locations = createMockLocations();


    @Override
    public List<Location> getAll() {
        return locations;
    }

    private static List<Location> createMockLocations() {
        final FhirContext fhirContext = FhirContext.forR4();
        try (InputStream is = MockScheduleRepository.class.getClassLoader().getResourceAsStream("example-locations.ndjson")) {

            final NDJSONToFHIR converter = new NDJSONToFHIR(fhirContext.newJsonParser());
            return converter
                    .inputStreamToResource(is)
                    .stream().map(r -> (Location) r)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Cannot open file");
        }
    }
}
