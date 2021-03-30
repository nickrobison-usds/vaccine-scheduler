package gov.usds.vaccineschedule.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * Created by nickrobison on 3/30/21
 */
@ConfigurationProperties(prefix = "vs.geocoder")
public class GeocoderConfig {

    private final String mapboxToken;

    @ConstructorBinding
    public GeocoderConfig(String mapboxToken) {
        this.mapboxToken = mapboxToken;
    }

    public String getMapboxToken() {
        return mapboxToken;
    }
}
