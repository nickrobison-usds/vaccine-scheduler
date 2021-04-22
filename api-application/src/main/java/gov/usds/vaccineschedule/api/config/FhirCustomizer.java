package gov.usds.vaccineschedule.api.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.FifoMemoryPagingProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.CorsInterceptor;
import ca.uhn.fhir.spring.boot.autoconfigure.FhirRestfulServerCustomizer;
import gov.usds.vaccineschedule.api.helpers.BaseURLProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;

/**
 * Created by nickrobison on 4/1/21
 */
@Configuration
public class FhirCustomizer implements FhirRestfulServerCustomizer {

    private final BaseURLProvider baseURLProvider;
    private final FhirContext ctx;
    private final CorsConfiguration cors;

    public FhirCustomizer(FhirContext ctx, BaseURLProvider baseURLProvider, CorsConfiguration cors) {
        this.ctx = ctx;
        this.baseURLProvider = baseURLProvider;
        this.cors = cors;
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

        final CorsInterceptor interceptor = new CorsInterceptor(this.cors);
        server.registerInterceptor(interceptor);
    }
}
