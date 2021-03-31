package gov.usds.vaccineschedule.api;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import gov.usds.vaccineschedule.api.services.ExampleDataService;
import gov.usds.vaccineschedule.api.utils.DbTruncator;
import gov.usds.vaccineschedule.api.utils.TestApplicationConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

/**
 * Created by nickrobison on 3/30/21
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestApplicationConfiguration.class)
public class BaseApplicationTest {

    @Autowired
    private DbTruncator truncator;
    @Autowired
    private ExampleDataService dataService;
    @Autowired
    protected FhirContext ctx;
    @LocalServerPort
    protected int port;


    @BeforeEach
    public void setup() {
        truncator.truncateAll();
        dataService.loadTestData();
    }

    @AfterEach
    public void cleanup() {
        truncator.truncateAll();
    }

    protected IGenericClient provideFhirClient() {
        return ctx.newRestfulGenericClient(String.format("http://localhost:%d/fhir", port));
    }
}
