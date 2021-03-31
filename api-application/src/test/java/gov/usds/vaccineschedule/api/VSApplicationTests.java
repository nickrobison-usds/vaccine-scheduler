package gov.usds.vaccineschedule.api;

import gov.usds.vaccineschedule.api.utils.TestApplicationConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Created by nickrobison on 3/30/21
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestApplicationConfiguration.class)
public class VSApplicationTests {

    @Test
    void contextLoads() {
        // no-op
    }
}
