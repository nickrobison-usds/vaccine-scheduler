package gov.usds.vaccineschedule.api.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.FifoMemoryPagingProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.spring.boot.autoconfigure.FhirRestfulServerCustomizer;
import gov.usds.vaccineschedule.api.helpers.BaseURLProvider;
import org.springframework.context.annotation.Configuration;

/**
 * Created by nickrobison on 4/1/21
 */
@Configuration
public class FhirCustomizer implements FhirRestfulServerCustomizer {

    private final BaseURLProvider baseURLProvider;
    private final FhirContext ctx;

    public FhirCustomizer(FhirContext ctx, BaseURLProvider baseURLProvider) {
        this.ctx = ctx;
        this.baseURLProvider = baseURLProvider;
    }

    @Override
    public void customize(RestfulServer server) {
        server.setFhirContext(this.ctx);
        final FifoMemoryPagingProvider provider = new FifoMemoryPagingProvider(100);
        server.setPagingProvider(provider);
        server.setServerAddressStrategy((servlet, req) -> this.baseURLProvider.get());
        // Should be configurable
        server.setMaximumPageSize(500);
        server.setDefaultPageSize(50);
    }
}
