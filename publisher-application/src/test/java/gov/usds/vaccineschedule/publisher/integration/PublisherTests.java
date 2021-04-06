package gov.usds.vaccineschedule.publisher.integration;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import gov.usds.vaccineschedule.publisher.models.PublishResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Created by nickrobison on 4/6/21
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PublisherTests {
    @Autowired
    private FhirContext ctx;
    @Autowired
    private WebTestClient webClient;
    @LocalServerPort
    private int port;

    @Test
    public void testSimplePublishing() {

        final PublishResponse response = webClient.get()
                .uri("/$bulk-publish")
                .exchange()
                .returnResult(PublishResponse.class)
                .getResponseBody()
                .blockFirst();
        assertNotNull(response, "Should have response");
        assertEquals(3, response.getOutput().size(), "Should have the correct files");

    }


    protected IGenericClient provideFhirClient() {
        return ctx.newRestfulGenericClient(String.format("http://localhost:%d/fhir", port));
    }
}
