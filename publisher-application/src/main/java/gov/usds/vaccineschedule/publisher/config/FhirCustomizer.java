package gov.usds.vaccineschedule.publisher.config;

import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.spring.boot.autoconfigure.FhirRestfulServerCustomizer;
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
