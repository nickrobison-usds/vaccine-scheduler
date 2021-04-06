package gov.usds.vaccineschedule.publisher.respositories;

import ca.uhn.fhir.context.FhirContext;
import gov.usds.vaccineschedule.common.helpers.NDJSONToFHIR;
import org.hl7.fhir.r4.model.Slot;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by nickrobison on 3/26/21
 */
@Service
public class MockSlotRepository implements SlotRepository {

    private static final List<Slot> slots = createMockSlots();


    @Override
    public List<Slot> getAll() {
        return slots;
    }

    private static List<Slot> createMockSlots() {
        final FhirContext fhirContext = FhirContext.forR4();
        try (InputStream is = MockScheduleRepository.class.getClassLoader().getResourceAsStream("example-slots.ndjson")) {

            final NDJSONToFHIR converter = new NDJSONToFHIR(fhirContext.newJsonParser());
            return converter
                    .inputStreamToResource(is)
                    .stream().map(r -> (Slot) r)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Cannot open file");
        }
    }
}
