package gov.usds.vaccineschedule.api.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * Created by nickrobison on 3/30/21
 */
@ConfigurationProperties(prefix = "vs.geocoder")
public class GeocoderConfigProperties {

    private final GeocoderEngine engine;
    private final String mapboxToken;

    @ConstructorBinding
    public GeocoderConfigProperties(GeocoderEngine engine, String mapboxToken) {
        this.engine = engine;
        this.mapboxToken = mapboxToken;
    }

    public String getMapboxToken() {
        return mapboxToken;
    }

    public GeocoderEngine getEngine() {
        return engine;
    }

    public enum GeocoderEngine {
        MAPBOX,
        POSTGRES,
        NO_OP
    }
}
