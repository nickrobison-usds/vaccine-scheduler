package cov.usds.vaccineshedule.publisher.respositories;

import ca.uhn.fhir.context.FhirContext;
import gov.usds.vaccineschedule.common.helpers.NDJSONToFHIR;
import org.hl7.fhir.r4.model.Schedule;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by nickrobison on 3/26/21
 */
@Service
public class MockScheduleRepository implements ScheduleRepository {

    private static final List<Schedule> schedules = createMockSchedules();

    @Override
    public List<Schedule> getAll() {
        return schedules;
    }

    private static List<Schedule> createMockSchedules() {
        final FhirContext fhirContext = FhirContext.forR4();
        try (InputStream is = MockScheduleRepository.class.getClassLoader().getResourceAsStream("example-schedules.ndjson")) {

            final NDJSONToFHIR converter = new NDJSONToFHIR(fhirContext.newJsonParser());
            return converter
                    .inputStreamToResource(is)
                    .stream().map(r -> (Schedule) r)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Cannot open file");
        }
    }
}
