package gov.usds.vaccineschedule.publisher.integration;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import gov.usds.vaccineschedule.common.helpers.NDJSONToFHIR;
import gov.usds.vaccineschedule.publisher.models.PublishResponse;
import gov.usds.vaccineschedule.publisher.utils.BaseApplicationTest;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static gov.usds.vaccineschedule.publisher.config.Constants.FHIR_NDJSON;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Created by nickrobison on 4/6/21
 */
public class PublisherTests extends BaseApplicationTest {

    private static final Map<String, Integer> expectedSizes = Map.of("Location", 10, "Schedule", 10, "Slot", 70);

    @Autowired
    private FhirContext ctx;
    @Autowired
    TestRestTemplate template;

    @Test
    public void testSimplePublishing() {
        final PublishResponse response = template.getForEntity("/$bulk-publish", PublishResponse.class).getBody();

        assertNotNull(response, "Should have response");
        assertEquals(3, response.getOutput().size(), "Should have the correct files");

        // For each file, fetch it
        final IParser parser = ctx.newJsonParser();
        final NDJSONToFHIR converter = new NDJSONToFHIR(parser);

        response.getOutput().forEach(o -> downloadOutputFile(converter, o));

    }

    private void downloadOutputFile(NDJSONToFHIR converter, PublishResponse.OutputEntry o) {

        // Set the correct headers
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", FHIR_NDJSON);

        final HttpEntity<Object> entity = new HttpEntity<>(httpHeaders);
        final ResponseEntity<String> ndjson = template
                .exchange(o.getUrl(), HttpMethod.GET, entity, String.class);
        assertAll(() -> assertEquals(HttpStatus.OK, ndjson.getStatusCode()),
                () -> assertNotNull(ndjson.getBody()));
        assertNotNull(ndjson, "Should have response");
        try (ByteArrayInputStream bos = new ByteArrayInputStream(ndjson.getBody().getBytes(StandardCharsets.UTF_8))) {
            final List<IBaseResource> resources = converter.inputStreamToResource(bos);
            assertEquals(expectedSizes.get(o.getType()), resources.size(), "Should have the correct number of resources");
        } catch (IOException e) {
            fail(e);
        }
    }
}
