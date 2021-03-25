package cov.usds.vaccineshedule.publisher.config;

import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.spring.boot.autoconfigure.FhirRestfulServerCustomizer;
import cov.usds.vaccineshedule.publisher.providers.PublishProvider;
import org.springframework.context.annotation.Configuration;

/**
 * Created by nickrobison on 3/25/21
 */
@Configuration
public class FhirCustomizer implements FhirRestfulServerCustomizer {

    @Override
    public void customize(RestfulServer server) {
//        server.registerProvider(new PublishProvider());
    }
}
