package cov.usds.vaccineshedule.publisher.respositories;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.Location;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by nickrobison on 3/25/21
 */
@Component
public class MockLocationRepository implements LocationRepository {

    private static final List<Location> locations = createMockLocations();


    @Override
    public List<Location> getAll() {
        return locations;
    }

    private static List<Location> createMockLocations() {
        final IParser parser = FhirContext.forR4().newJsonParser();
        final URL exampleFile = MockLocationRepository.class.getClassLoader().getResource("example-locations.ndjson");

        if (exampleFile == null) {
            throw new RuntimeException("Cannot find example file");
        }

        try {
            try (Stream<String> lines = Files.lines(Paths.get(exampleFile.toURI()))) {
                return lines.map(line -> (Location) parser.parseResource(line))
                        .collect(Collectors.toList());
            }

        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
