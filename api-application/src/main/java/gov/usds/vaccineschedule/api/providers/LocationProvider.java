package gov.usds.vaccineschedule.api.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jaxrs.server.AbstractJaxRsResourceProvider;
import ca.uhn.fhir.rest.annotation.Search;
import gov.usds.vaccineschedule.api.services.LocationService;
import org.hl7.fhir.r4.model.Location;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Created by nickrobison on 3/26/21
 */
@Component
public class LocationProvider extends AbstractJaxRsResourceProvider<Location> {

    private final LocationService service;

    public LocationProvider(FhirContext ctx, LocationService service) {
        super(ctx);
        this.service = service;
    }

    @Search
    public Collection<Location> locationSearch() {
        return service.getLocations();
    }

    @Override
    public Class<Location> getResourceType() {
        return Location.class;
    }
}
