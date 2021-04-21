package gov.usds.vaccineschedule.api.providers;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IOperationUntypedWithInput;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import gov.usds.vaccineschedule.api.BaseApplicationTest;
import gov.usds.vaccineschedule.common.models.VaccineSlot;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Schedule;
import org.hl7.fhir.r4.model.Slot;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.Instant;
import java.util.List;

import static gov.usds.vaccineschedule.api.utils.FhirHandlers.unwrapBundle;
import static gov.usds.vaccineschedule.common.Constants.LOCATION_PROFILE;
import static gov.usds.vaccineschedule.common.Constants.ORIGINAL_ID_SYSTEM;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by nickrobison on 3/31/21
 */
public class SlotProviderTest extends BaseApplicationTest {

    @Test
    void testAllSlotsWithPagination() {
        final java.util.Date searchTime = Date.from(Instant.now());
        final IGenericClient client = provideFhirClient();

        Bundle slots = client
                .search()
                .forResource(Slot.class)
                .preferResponseType(VaccineSlot.class)
                .returnBundle(Bundle.class)
                .count(10)
                .encodedJson()
                .execute();

        final List<VaccineSlot> vSlots = unwrapBundle(client, slots, searchTime);

        final long uniqueIds = vSlots.stream().map(Resource::getId).distinct().count();
        assertAll(() -> assertEquals(70, vSlots.size(), "Should have all the slots"),
                () -> assertEquals(70, uniqueIds, "Should have all unique IDs"));
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
        assertEquals(LOCATION_PROFILE, location.getMeta().getProfile().get(0).getValueAsString(), "Should have asserted profile");

        final Bundle execute = client
                .search()
                .forResource(Slot.class)
                .where(Slot.SCHEDULE.hasChainedProperty(Schedule.ACTOR.hasAnyOfIds(location.getIdElement().getIdPart())))
                .returnBundle(Bundle.class)
                .encodedJson()
                .execute();

        assertEquals(7, execute.getEntry().size(), "Should have a subset of values");
    }

    @Test
    void testSlotsForLocationAndTime() {
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

        final Bundle boundedBundle = client
                .search()
                .forResource(Slot.class)
                .where(Slot.SCHEDULE.hasChainedProperty(Schedule.ACTOR.hasId(location.getIdElement().getIdPart())))
                .and(Slot.START.after().day(Date.valueOf("2021-03-02")))
                .and(Slot.START.before().day(Date.valueOf("2021-03-04")))
                .returnBundle(Bundle.class)
                .encodedJson()
                .execute();

        assertEquals(2, boundedBundle.getEntry().size(), "Should have a subset of values");

        final Bundle unboundedBundle = client
                .search()
                .forResource(Slot.class)
                .where(Slot.SCHEDULE.hasChainedProperty(Schedule.ACTOR.hasId(location.getIdElement().getIdPart())))
                .and(Slot.START.afterOrEquals().day(Date.valueOf("2021-03-04")))
                .returnBundle(Bundle.class)
                .encodedJson()
                .execute();

        assertEquals(5, unboundedBundle.getEntry().size(), "Should have a subset of values");
    }

    @Test
    void testSlotExtensions() {
        final IGenericClient client = provideFhirClient();

        final Bundle singleSlot = client
                .search()
                .forResource(Slot.class)
                .where(Slot.IDENTIFIER.exactly().systemAndCode(ORIGINAL_ID_SYSTEM, "Slot/28"))
                .returnBundle(Bundle.class)
                .preferResponseType(VaccineSlot.class)
                .encodedJson()
                .execute();

        final VaccineSlot single = (VaccineSlot) singleSlot.getEntry().get(0).getResource();
        assertAll(() -> assertEquals("https://ehr-portal.example.org/bookings?slot=1000008", single.getBookingUrl().asStringValue(), "Should have correct URL"),
                () -> assertEquals(100, single.getCapacity().getValue(), "Should have correct value"),
                () -> assertTrue(single.getBookingPhone().isEmpty(), "Should not have phone number"));
    }

    @Test
    void testValidation() {
        final Slot invalidSlot = new Slot();
        invalidSlot.setId("test-slot");
        final IGenericClient client = provideFhirClient();

        final IOperationUntypedWithInput<MethodOutcome> validateRequest = client
                .operation()
                .onType(Slot.class)
                .named("$validate")
                .withParameter(Parameters.class, "resource", invalidSlot)
                .returnMethodOutcome();

        final UnprocessableEntityException exn = assertThrows(UnprocessableEntityException.class, validateRequest::execute, "Should fail validation");
        final OperationOutcome outcome = (OperationOutcome) exn.getOperationOutcome();
        assertEquals(4, outcome.getIssue().size(), "Should have the correct number of issues");
    }
}
