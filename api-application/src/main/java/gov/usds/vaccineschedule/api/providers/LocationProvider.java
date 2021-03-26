package gov.usds.vaccineschedule.api.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jaxrs.server.AbstractJaxRsResourceProvider;
import ca.uhn.fhir.rest.annotation.Search;
import gov.usds.vaccineschedule.api.db.models.LocationEntity;
import gov.usds.vaccineschedule.api.repositories.LocationRepository;
import org.hl7.fhir.r4.model.Location;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by nickrobison on 3/26/21
 */
@Component
public class LocationProvider extends AbstractJaxRsResourceProvider<Location> {

    private final LocationRepository repo;

    public LocationProvider(FhirContext ctx, LocationRepository repo) {
        super(ctx);
        this.repo = repo;
    }

    @Search
    public Collection<Location> locationSearch() {
        final Iterable<LocationEntity> all = this.repo.findAll();
        return StreamSupport.stream(all.spliterator(), false)
                .map(LocationEntity::toFHIR)
                .collect(Collectors.toList());
    }

    @Override
    public Class<Location> getResourceType() {
        return Location.class;
    }
}
