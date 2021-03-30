package gov.usds.vaccineschedule.api.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IQueryParameterType;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.param.TokenParam;
import gov.usds.vaccineschedule.api.BaseApplicationTest;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Location;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by nickrobison on 3/30/21
 */
public class LocationProviderTest extends BaseApplicationTest {

    @Autowired
    private FhirContext ctx;

    @LocalServerPort
    private int port;

    @Test
    public void testLocationEverything() {
        final IGenericClient client = ctx.newRestfulGenericClient(String.format("http://localhost:%d/fhir", port));

        final Bundle results = client.search()
                .forResource(Location.class)
                .returnBundle(Bundle.class)
                .encodedJson()
                .execute();

        assertEquals(10, results.getTotal(), "Should have all the results");
    }

    @Test
    public void testLocationSearch() {
        final IGenericClient client = ctx.newRestfulGenericClient(String.format("http://localhost:%d/fhir", port));
        Map<String, List<IQueryParameterType>> params = new HashMap<>();
        params.put("near", List.of(new TokenParam().setValue("42.4887|-71.2837")));

        final Bundle results = client.search()
                .forResource(Location.class)
                .where(params)
                .returnBundle(Bundle.class)
                .encodedJson()
                .execute();

        assertEquals(7, results.getEntry().size(), "Should equal");
    }
}
