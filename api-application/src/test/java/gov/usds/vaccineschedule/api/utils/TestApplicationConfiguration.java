package gov.usds.vaccineschedule.api.utils;

import gov.usds.vaccineschedule.api.services.geocoder.GeocoderService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;

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

    @Bean
    public CorsConfiguration provideTestCors() {
        // Define your CORS configuration. This is an example
        // showing a typical setup. You should customize this
        // to your specific needs
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedHeader("x-fhir-starter");
        config.addAllowedHeader("Origin");
        config.addAllowedHeader("Accept");
        config.addAllowedHeader("X-Requested-With");
        config.addAllowedHeader("Content-Type");

        config.addAllowedOrigin("*");

        config.addExposedHeader("Location");
        config.addExposedHeader("Content-Location");
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        return config;
    }
}
