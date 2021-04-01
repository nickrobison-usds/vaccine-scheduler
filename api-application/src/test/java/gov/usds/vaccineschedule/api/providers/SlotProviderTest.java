package gov.usds.vaccineschedule.api.providers;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import gov.usds.vaccineschedule.api.BaseApplicationTest;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Schedule;
import org.hl7.fhir.r4.model.Slot;
import org.junit.jupiter.api.Test;

import static gov.usds.vaccineschedule.api.db.models.Constants.ORIGINAL_ID_SYSTEM;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by nickrobison on 3/31/21
 */
public class SlotProviderTest extends BaseApplicationTest {

    @Test
    void testAllSlots() {
        final IGenericClient client = provideFhirClient();

        final Bundle slots = client
                .search()
                .forResource(Slot.class)
                .returnBundle(Bundle.class)
                .encodedJson()
                .execute();

        assertEquals(70, slots.getEntry().size(), "Should have all the slots");
    }

    @Test
    void testSlotsForLocation() {
        final IGenericClient client = provideFhirClient();

        final Bundle firstLocation = client
                .search()
                .forResource(Location.class)
                .where(Location.IDENTIFIER.exactly().systemAndCode(ORIGINAL_ID_SYSTEM, "Location/1"))
                .returnBundle(Bundle.class)
                .encodedJson()
                .execute();

        assertEquals(1, firstLocation.getEntry().size(), "Should only have a single location");
        final Location location = (Location) firstLocation.getEntry().get(0).getResource();

        final Bundle execute = client
                .search()
                .forResource(Slot.class)
                .where(Slot.SCHEDULE.hasChainedProperty(Schedule.ACTOR.hasId(location.getIdElement().getIdPart())))
                .returnBundle(Bundle.class)
                .encodedJson()
                .execute();

        assertEquals(7, execute.getEntry().size(), "Should have a subset of values");
    }
}
