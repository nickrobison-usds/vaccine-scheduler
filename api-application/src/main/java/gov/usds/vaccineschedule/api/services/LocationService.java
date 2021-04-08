package gov.usds.vaccineschedule.api.services;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationFailureException;
import ca.uhn.fhir.validation.ValidationOptions;
import ca.uhn.fhir.validation.ValidationResult;
import gov.usds.vaccineschedule.api.db.models.LocationEntity;
import gov.usds.vaccineschedule.api.models.NearestQuery;
import gov.usds.vaccineschedule.api.repositories.LocationRepository;
import gov.usds.vaccineschedule.api.services.geocoder.GeocoderService;
import gov.usds.vaccineschedule.common.Constants;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Location;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static gov.usds.vaccineschedule.api.repositories.LocationRepository.hasIdentifier;
import static gov.usds.vaccineschedule.api.repositories.LocationRepository.inCity;
import static gov.usds.vaccineschedule.api.repositories.LocationRepository.inH3Indexes;
import static gov.usds.vaccineschedule.api.repositories.LocationRepository.inPostalCode;
import static gov.usds.vaccineschedule.api.repositories.LocationRepository.inState;
import static gov.usds.vaccineschedule.api.services.ServiceHelpers.fromIterable;
import static gov.usds.vaccineschedule.common.Constants.ORIGINAL_ID_SYSTEM;

/**
 * Created by nickrobison on 3/26/21
 */
@Service
@Transactional(readOnly = true)
public class LocationService {

    private final LocationRepository repo;
    private final GeocoderService geocoder;
    private final FhirValidator validator;
    private final FhirContext ctx;
    private final H3Service h3;

    public LocationService(LocationRepository repo, GeocoderService geocoder, FhirValidator validator, FhirContext ctx, H3Service h3) {
        this.repo = repo;
        this.geocoder = geocoder;
        this.validator = validator;
        this.ctx = ctx;
        this.h3 = h3;
    }

    @Transactional
    public Collection<Location> addLocations(Collection<Location> locations) {
        return locations
                .stream()
                .map(this::addLocation)
                .collect(Collectors.toList());
    }

    @Transactional
    public Location addLocation(Location location) {
        this.validateLocation(location);
        final LocationEntity entity = LocationEntity.fromFHIR(location);
        // This shouldn't run on each load, only if things change.
        final Point point = this.geocoder.geocodeLocation(entity.getAddress()).block();
        if (point != null) {
            entity.setCoordinates(point);
            entity.setH3Index(this.h3.encodePoint(point));
        }

        // Determine if the location already exists, by searching
        final Optional<LocationEntity> maybeExists = this.repo.findOne(hasIdentifier(ORIGINAL_ID_SYSTEM, location.getId()));
        if (maybeExists.isPresent()) {
            final LocationEntity exists = maybeExists.get();
            exists.merge(entity);
            return this.repo.save(exists).toFHIR();
        } else {
            return this.repo.save(entity).toFHIR();
        }

    }

    public Optional<Location> getLocation(IIdType id) {
        final UUID locID = UUID.fromString(id.getIdPart());
        return this.repo.findById(locID).map(LocationEntity::toFHIR);
    }

    public long countLocations(@Nullable TokenParam identifier, @Nullable StringParam city, @Nullable StringParam state, @Nullable StringParam postalCode) {
        final Optional<Specification<LocationEntity>> optional = buildLocationSpec(identifier, city, state, postalCode);
        return optional.map(this.repo::count).orElseGet(this.repo::count);
    }

    public List<Location> findLocations(@Nullable TokenParam identifier, @Nullable StringParam city, @Nullable StringParam state, @Nullable StringParam postalCode, Pageable page) {
        Supplier<Iterable<LocationEntity>> supplier;
        final Optional<Specification<LocationEntity>> optional = buildLocationSpec(identifier, city, state, postalCode);
        supplier = optional.<Supplier<Iterable<LocationEntity>>>map(specification -> () -> this.repo.findAll(specification, page)).orElseGet(() -> () -> this.repo.findAll(page));
        return StreamSupport.stream(supplier.get().spliterator(), false)
                .map(LocationEntity::toFHIR)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<Location> findByLocation(NearestQuery point, Pageable pageable) {
        final Iterable<LocationEntity> locationEntities;
        if (this.h3.useH3Search()) {
            final List<Long> neighbors = this.h3.findNeighbors(point);
            locationEntities = this.repo.findAll(inH3Indexes(neighbors), pageable);
        } else {
            final double distance = point.getDistanceInMeters();
            locationEntities = this.repo.locationsWithinDistance(point.getPoint(), distance, pageable);
        }
        return fromIterable(() -> locationEntities, LocationEntity::toFHIR);
    }

    @Transactional
    public long countByLocation(NearestQuery point) {
        if (this.h3.useH3Search()) {
            final List<Long> neighbors = this.h3.findNeighbors(point);
            return this.repo.count(inH3Indexes(neighbors));
        }
        final double distance = point.getDistanceInMeters();
        return this.repo.countLocationsWithinDistance(point.getPoint(), distance);
    }

    private Optional<Specification<LocationEntity>> buildLocationSpec(@Nullable TokenParam identifier, @Nullable StringParam city, @Nullable StringParam state, @Nullable StringParam postalCode) {

        List<Specification<LocationEntity>> specifications = new ArrayList<>();
        if (identifier != null) {
            specifications.add(hasIdentifier(identifier.getSystem(), identifier.getValue()));
        }
        if (city != null) {
            specifications.add(inCity(city.getValue()));
        }
        if (state != null) {
            specifications.add(inState(state.getValue()));
        }

        if (postalCode != null) {
            specifications.add(inPostalCode(postalCode.getValue()));
        }

        // Combine everything using where/and
        if (!specifications.isEmpty()) {
            final Specification<LocationEntity> where = Specification.where(specifications.get(0));
            final Specification<LocationEntity> combined = specifications
                    .subList(1, specifications.size())
                    .stream().reduce(where, Specification::and);
            return Optional.of(combined);
        }
        return Optional.empty();
    }

    private void validateLocation(Location location) {
        final ValidationOptions options = new ValidationOptions();
        options.addProfile(Constants.LOCATION_PROFILE);

        final ValidationResult result = this.validator.validateWithResult(location, options);
        if (!result.isSuccessful()) {
            throw new ValidationFailureException(this.ctx, result.toOperationOutcome());
        }
    }
}
