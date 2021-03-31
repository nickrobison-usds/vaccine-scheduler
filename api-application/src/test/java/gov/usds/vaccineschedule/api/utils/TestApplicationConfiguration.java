package gov.usds.vaccineschedule.api.utils;

import gov.usds.vaccineschedule.api.services.geocoder.GeocoderService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Created by nickrobison on 3/31/21
 */
@TestConfiguration
public class TestApplicationConfiguration {

    @Bean
    @Primary
    public GeocoderService testGeocoder() {
        return new StaticTestGeocoder();
    }
}
