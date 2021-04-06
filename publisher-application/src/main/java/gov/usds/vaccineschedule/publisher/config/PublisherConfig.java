package gov.usds.vaccineschedule.publisher.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * Created by nickrobison on 4/6/21
 */
@ConfigurationProperties(prefix = "publisher")
public class PublisherConfig {

    private final String baseUrl;

    @ConstructorBinding
    public PublisherConfig(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseURL() {
        return baseUrl;
    }
}
