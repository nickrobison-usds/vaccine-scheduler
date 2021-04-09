package gov.usds.vaccineschedule.api.config;

import gov.usds.vaccineschedule.api.properties.GeocoderConfigProperties;
import gov.usds.vaccineschedule.api.services.geocoder.DBGeocoderService;
import gov.usds.vaccineschedule.api.services.geocoder.GeocoderService;
import gov.usds.vaccineschedule.api.services.geocoder.MapBoxGeocoderService;
import gov.usds.vaccineschedule.api.services.geocoder.NoOpGeocoder;
import gov.usds.vaccineschedule.api.services.geocoder.TigerRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by nickrobison on 4/9/21
 */
@Configuration
public class GeocoderConfig {

    @Bean
    @ConditionalOnProperty(value = "vs.geocoder.engine", havingValue = "postgres")
    public GeocoderService provideGeocoder(TigerRepository repo) {
        return new DBGeocoderService(repo);
    }

    @Bean
    @ConditionalOnProperty(value = "vs.geocoder.engine", havingValue = "mapbox")
    public GeocoderService provideMapboxCoder(GeocoderConfigProperties config) {
        return new MapBoxGeocoderService(config);
    }

    @Bean
    @ConditionalOnProperty(value = "vs.geocoder.engine", havingValue = "no_op")
    public GeocoderService provideNoOpGeocoder() {
        return new NoOpGeocoder();
    }


}
