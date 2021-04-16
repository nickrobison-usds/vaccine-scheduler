package gov.usds.vaccineschedule.wapublisher.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * Created by nickrobison on 4/16/21
 */
@ConfigurationProperties(prefix = "publisher")
public class PublisherConfigProperties {

    private final String baseUrl;

    @ConstructorBinding
    public PublisherConfigProperties(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseURL() {
        return baseUrl;
    }
}
