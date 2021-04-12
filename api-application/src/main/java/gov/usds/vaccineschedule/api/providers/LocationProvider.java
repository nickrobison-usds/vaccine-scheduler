package gov.usds.vaccineschedule.api.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.Count;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Offset;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.validation.FhirValidator;
import gov.usds.vaccineschedule.api.helpers.BaseURLProvider;
import gov.usds.vaccineschedule.api.models.NearestQuery;
import gov.usds.vaccineschedule.api.pagination.AbstractPaginatingAndValidatingProvider;
import gov.usds.vaccineschedule.api.services.LocationService;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.Location;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static gov.usds.vaccineschedule.common.Constants.LOCATION_PROFILE;

/**
 * Created by nickrobison on 3/26/21
 */
@Component
public class LocationProvider extends AbstractPaginatingAndValidatingProvider<Location> {

    private final LocationService service;

    public LocationProvider(FhirContext ctx, FhirValidator validator, LocationService service, BaseURLProvider provider) {
        super(ctx, validator, provider);
        this.service = service;
    }

    @Search
    public Bundle locationSearch(
            @OptionalParam(name = Location.SP_NEAR) TokenParam nearestParam,
            @OptionalParam(name = Location.SP_IDENTIFIER) TokenParam identifier,
            @OptionalParam(name = Location.SP_ADDRESS_CITY) StringParam city,
            @OptionalParam(name = Location.SP_ADDRESS_STATE) StringParam state,
            @OptionalParam(name = Location.SP_ADDRESS_POSTALCODE) StringParam postalCode,
            RequestDetails requestDetails,
            @Offset Integer pageOffset,
            @Count Integer pageSize) {
        // If we have a nearestParam, do that, rather than anything else
        final Pageable pageRequest = super.buildPageRequest(pageOffset, pageSize);

        final InstantType searchTime = InstantType.now();
        final long totalCount;
        List<Location> locations;
        if (nearestParam != null) {
            final NearestQuery query = NearestQuery.fromToken(nearestParam.getValue());
            totalCount = this.service.countByLocation(query);
            locations = this.service.findByLocation(query, pageRequest);
        } else {
            totalCount = this.service.countLocations(identifier, city, state, postalCode);
            locations = service.findLocations(identifier, city, state, postalCode, pageRequest);
        }

        return super.createBundle(requestDetails, locations, searchTime, pageRequest, totalCount);
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

    @Override
    public String getResourceProfile() {
        return LOCATION_PROFILE;
    }
}
