package gov.usds.vaccineschedule.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;

/**
 * Created by nickrobison on 4/12/21
 */
@Profile({"dev", "dockerized"})
@Configuration
public class DevCorsConfiguration {

    @Bean
    public CorsConfiguration provideDevCors() {
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
