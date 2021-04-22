package gov.usds.vaccineschedule.api.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jaxrs.server.AbstractJaxRsResourceProvider;
import ca.uhn.fhir.rest.annotation.Search;
import gov.usds.vaccineschedule.api.properties.ScheduleSourceConfigProperties;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.codesystems.EndpointConnectionType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by nickrobison on 4/22/21
 */
@Component
public class EndpointProvider extends AbstractJaxRsResourceProvider<Endpoint> {

    private final ScheduleSourceConfigProperties config;

    public EndpointProvider(FhirContext ctx, ScheduleSourceConfigProperties config) {
        super(ctx);
        this.config = config;
    }


    @Search
    public List<Endpoint> endpointSearch() {
        return config.getSources()
                .stream()
                .map(source -> {
                    final Endpoint endpoint = new Endpoint();
                    endpoint.setId(UUID.randomUUID().toString());
                    endpoint.setStatus(Endpoint.EndpointStatus.ACTIVE);
                    endpoint.setConnectionType(new Coding().setCode(EndpointConnectionType.HL7FHIRREST.toCode()));
                    endpoint.setAddress(source);
                    return endpoint;
                })
                .collect(Collectors.toList());
    }


    @Override
    public Class<Endpoint> getResourceType() {
        return Endpoint.class;
    }
}
