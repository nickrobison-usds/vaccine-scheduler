package gov.usds.vaccineschedule.wapublisher;

import gov.usds.vaccineschedule.wapublisher.properties.PublisherConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Created by nickrobison on 4/16/21
 */
@SpringBootApplication
@EnableConfigurationProperties({
        PublisherConfigProperties.class
})
public class WAPublisherApplication {

    public static void main(String[] args) {
        SpringApplication.run(WAPublisherApplication.class, args);
    }
}
