package gov.usds.vaccineschedule.api.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;

/**
 * Created by nickrobison on 4/22/21
 */
@Configuration
public class ProdCorsConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CorsConfiguration provideProdCors() {
        // Define your CORS configuration. This is an example
        // showing a typical setup. You should customize this
        // to your specific needs
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedHeader("x-fhir-starter");
        config.addAllowedHeader("Origin");
        config.addAllowedHeader("Accept");
        config.addAllowedHeader("X-Requested-With");
        config.addAllowedHeader("Content-Type");

        config.addAllowedOrigin("https://trusting-lamarr-0ec82b.netlify.app");

        config.addExposedHeader("Location");
        config.addExposedHeader("Content-Location");
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        return config;
    }
}
