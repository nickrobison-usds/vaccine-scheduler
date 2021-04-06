package gov.usds.vaccineschedule.publisher;

import gov.usds.vaccineschedule.publisher.config.PublisherConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Created by nickrobison on 3/25/21
 */
@SpringBootApplication
@EnableConfigurationProperties({PublisherConfig.class})
public class SchedulePublisherApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulePublisherApplication.class, args);
    }
}
