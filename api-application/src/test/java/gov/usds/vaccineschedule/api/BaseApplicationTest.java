package gov.usds.vaccineschedule.api;

import gov.usds.vaccineschedule.api.services.ExampleDataService;
import gov.usds.vaccineschedule.api.utils.DbTruncator;
import gov.usds.vaccineschedule.api.utils.TestApplicationConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

    @BeforeEach
    public void setup() {
        truncator.truncateAll();
        dataService.loadTestData();
    }

    @AfterEach
    public void cleanup() {
        truncator.truncateAll();
    }
}
