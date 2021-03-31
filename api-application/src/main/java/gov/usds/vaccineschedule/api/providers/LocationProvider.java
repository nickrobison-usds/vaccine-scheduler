package gov.usds.vaccineschedule.api.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jaxrs.server.AbstractJaxRsResourceProvider;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import gov.usds.vaccineschedule.api.services.LocationService;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Location;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
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
    public Collection<Location> locationNearest(@OptionalParam(name = Location.SP_NEAR) TokenParam nearestParam, @OptionalParam(name = Location.SP_IDENTIFIER) TokenParam identifier) {
        if (nearestParam != null) {
            final String[] values = nearestParam.getValue().split("\\|");
            final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
            final Point point = factory.createPoint(new Coordinate(Double.parseDouble(values[1]), Double.parseDouble(values[0])));
            return this.service.findByLocation(point);
        } else if (identifier != null) {
            return service.findByIdentifier(identifier);
        }
        return service.getLocations();
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
