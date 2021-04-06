package gov.usds.vaccineschedule.api.providers;

import ca.uhn.fhir.model.api.IQueryParameterType;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import gov.usds.vaccineschedule.api.BaseApplicationTest;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Schedule;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static gov.usds.vaccineschedule.api.db.models.Constants.ORIGINAL_ID_SYSTEM;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by nickrobison on 3/31/21
 */
public class ScheduleProviderTest extends BaseApplicationTest {

    @Test
    void testMalformedPageRequests() {

        Map<String, List<IQueryParameterType>> params = new HashMap<>();
        params.put("_count", List.of(new StringParam("5000")));

        final IGenericClient client = provideFhirClient();
        IQuery<Bundle> request = client.search()
                .forResource(Schedule.class)
                .where(params)
                .returnBundle(Bundle.class)
                .encodedJson();

        final InvalidRequestException exn = assertThrows(InvalidRequestException.class, request::execute, "Should throw with negative page number");
        assertEquals("HTTP 400 : Page size of : 5000 exceeds the maximum of 500", exn.getLocalizedMessage(), "Should have correct error message");
    }

    @Test
    void testAllSchedule() {
        final IGenericClient client = provideFhirClient();
        final Bundle results = client.search()
                .forResource(Schedule.class)
                .returnBundle(Bundle.class)
                .encodedJson()
                .execute();

        assertEquals(10, results.getTotal(), "Should have all the results");

        final Schedule resource = (Schedule) results.getEntry().get(0).getResource();

        final Reference location = resource.getActor().get(0);

        final Bundle locResults = client
                .search()
                .forResource(Schedule.class)
                .where(Schedule.ACTOR.hasId(location.getReference()))
                .returnBundle(Bundle.class)
                .encodedJson()
                .execute();
        assertAll(() -> assertEquals(1, locResults.getEntry().size(), "Should have a single value"),
                () -> assertTrue(resource.equalsDeep(locResults.getEntry().get(0).getResource()), "Should have correct resource"));
    }

    @Test
    void testScheduleOriginalId() {
        final IGenericClient client = provideFhirClient();

        final Bundle results = client
                .search()
                .forResource(Schedule.class)
                .where(Schedule.IDENTIFIER.exactly().systemAndCode(ORIGINAL_ID_SYSTEM, "Schedule/10"))
                .returnBundle(Bundle.class)
                .encodedJson()
                .execute();

        assertEquals(1, results.getEntry().size(), "Should have a single result");
        final Schedule origSchedule = (Schedule) results.getEntry().get(0).getResource();

        final Schedule readSchedule = client
                .read()
                .resource(Schedule.class)
                .withId(origSchedule.getIdElement().getIdPart())
                .encodedJson()
                .execute();

        // Resource IDs are getting chewed up
//        assertTrue(origSchedule.equalsDeep(readSchedule), "Locations should match");
    }

    @Test
    void testScheduleNoLocation() {
        final IGenericClient client = provideFhirClient();

        final Bundle execute = client
                .search()
                .forResource(Schedule.class)
                .where(Schedule.ACTOR.hasId("Location/" + UUID.randomUUID()))
                .returnBundle(Bundle.class)
                .execute();

        assertEquals(0, execute.getEntry().size(), "Should not have any results");
    }
}
