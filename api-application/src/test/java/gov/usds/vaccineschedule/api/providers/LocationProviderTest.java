package gov.usds.vaccineschedule.api.providers;

import ca.uhn.fhir.model.api.IQueryParameterType;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.param.TokenParam;
import gov.usds.vaccineschedule.api.BaseApplicationTest;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Location;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gov.usds.vaccineschedule.api.db.models.Constants.ORIGINAL_ID_SYSTEM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by nickrobison on 3/30/21
 */
public class LocationProviderTest extends BaseApplicationTest {

    @Test
    public void testLocationEverything() {
        final IGenericClient client = provideFhirClient();
        final Bundle results = client.search()
                .forResource(Location.class)
                .returnBundle(Bundle.class)
                .encodedJson()
                .execute();

        assertEquals(10, results.getTotal(), "Should have all the results");
    }

    @Test
    public void testLocationSearch() {
        final IGenericClient client = provideFhirClient();
        Map<String, List<IQueryParameterType>> params = new HashMap<>();
        params.put("near", List.of(new TokenParam().setValue("42.4887|-71.2837|50")));

        final Bundle results = client.search()
                .forResource(Location.class)
                .where(params)
                .returnBundle(Bundle.class)
                .encodedJson()
                .execute();

        assertEquals(7, results.getEntry().size(), "Should equal");
    }

    @Test
    public void testLocationOriginalId() {
        final IGenericClient client = provideFhirClient();

        final Bundle results = client
                .search()
                .forResource(Location.class)
                .where(Location.IDENTIFIER.exactly().systemAndCode(ORIGINAL_ID_SYSTEM, "Location/1"))
                .returnBundle(Bundle.class)
                .encodedJson()
                .execute();

        assertEquals(1, results.getEntry().size(), "Should have a single result");
        final Location origLocation = (Location) results.getEntry().get(0).getResource();

        final Location readLocation = client
                .read()
                .resource(Location.class)
                .withId(origLocation.getIdElement().getIdPart())
                .encodedJson()
                .execute();

        assertTrue(origLocation.equalsDeep(readLocation), "Locations should match");
    }

    @Test
    void cityStateQuery() {
        final IGenericClient client = provideFhirClient();
        // Look for all by state

        final Bundle maLocations = client
                .search()
                .forResource(Location.class)
                .where(Location.ADDRESS_STATE.matchesExactly().value("MA"))
                .returnBundle(Bundle.class)
                .encodedJson()
                .execute();

        assertEquals(10, maLocations.getEntry().size(), "Should have all locations");

        final Location first = (Location) maLocations.getEntry().get(0).getResource();

        // Search for a given city
        final Bundle cityLocations = client
                .search()
                .forResource(Location.class)
                .where(Location.ADDRESS_STATE.matchesExactly().value(first.getAddress().getState()))
                .and(Location.ADDRESS_CITY.matchesExactly().value(first.getAddress().getCity()))
                .returnBundle(Bundle.class)
                .encodedJson()
                .execute();

        assertEquals(1, cityLocations.getEntry().size(), "Should have a single location");

        // City with wrong state
        final Bundle badStateLocations = client
                .search()
                .forResource(Location.class)
                .where(Location.ADDRESS_STATE.matchesExactly().value("IN"))
                .and(Location.ADDRESS_CITY.matchesExactly().value(first.getAddress().getCity()))
                .returnBundle(Bundle.class)
                .encodedJson()
                .execute();

        assertEquals(0, badStateLocations.getEntry().size(), "Should not have any locations");

        // City with wrong state
        final Bundle wrongCity = client
                .search()
                .forResource(Location.class)
                .where(Location.ADDRESS_STATE.matchesExactly().value(first.getAddress().getState()))
                .and(Location.ADDRESS_CITY.matchesExactly().value("Nowhere")) // Actually a city in CO
                .returnBundle(Bundle.class)
                .encodedJson()
                .execute();

        assertEquals(0, wrongCity.getEntry().size(), "Should have a single location");
    }
}
