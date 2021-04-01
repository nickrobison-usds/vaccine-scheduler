package gov.usds.vaccineschedule.api.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jaxrs.server.AbstractJaxRsResourceProvider;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import gov.usds.vaccineschedule.api.models.NearestQuery;
import gov.usds.vaccineschedule.api.services.LocationService;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Location;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

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
    public Collection<Location> locationSearch(
            @OptionalParam(name = Location.SP_NEAR) TokenParam nearestParam,
            @OptionalParam(name = Location.SP_IDENTIFIER) TokenParam identifier,
            @OptionalParam(name = Location.SP_ADDRESS_CITY) StringParam city,
            @OptionalParam(name = Location.SP_ADDRESS_STATE) StringParam state) {
        // If we have a nearestParam, do that, rather than anything else
        if (nearestParam != null) {
            final NearestQuery query = NearestQuery.fromToken(nearestParam.getValue());
            return this.service.findByLocation(query);
        } else {
            return service.findLocations(identifier, city, state);
        }
    }

    @Read
    public Location getLocationById(@IdParam IIdType id) {
        final Optional<Location> location = this.service.getLocation(id);
        return location.orElseThrow(() -> new ResourceNotFoundException(id));
    }

    @Override
    public Class<Location> getResourceType() {
        return Location.class;
    }
}
