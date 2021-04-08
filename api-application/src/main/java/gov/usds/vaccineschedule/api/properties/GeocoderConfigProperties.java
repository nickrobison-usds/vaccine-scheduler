package gov.usds.vaccineschedule.api.properties;

import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * Created by nickrobison on 3/30/21
 */
public class GeocoderConfigProperties {

    private final String mapboxToken;

    @ConstructorBinding
    public GeocoderConfigProperties(String mapboxToken) {
        this.mapboxToken = mapboxToken;
    }

    public String getMapboxToken() {
        return mapboxToken;
    }
}
