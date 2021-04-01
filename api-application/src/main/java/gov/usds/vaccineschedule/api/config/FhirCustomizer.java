package gov.usds.vaccineschedule.api.config;

import ca.uhn.fhir.rest.server.FifoMemoryPagingProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.spring.boot.autoconfigure.FhirRestfulServerCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Created by nickrobison on 4/1/21
 */
@Configuration
public class FhirCustomizer implements FhirRestfulServerCustomizer {

    private final Environment environment;

    public FhirCustomizer(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void customize(RestfulServer server) {
        final FifoMemoryPagingProvider provider = new FifoMemoryPagingProvider(100);
        server.setPagingProvider(provider);
        server.setServerAddressStrategy((servlet, req) -> {
            // If we have a base url, we should use it. Otherwise, build one using localhost and the current port
            final String baseUrl = environment.getProperty("vs.baseUrl");
            if (baseUrl != null) {
                return baseUrl;
            }
            return String.format("http://localhost:%s/fhir/", environment.getProperty("local.server.port"));
        });
    }
}
