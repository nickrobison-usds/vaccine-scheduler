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
import gov.usds.vaccineschedule.common.models.VaccineLocation;
import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Location;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.units.indriya.quantity.Quantities;

import javax.annotation.Nullable;
import javax.measure.Quantity;
import javax.measure.quantity.Length;
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
import static gov.usds.vaccineschedule.common.Constants.ORIGINAL_ID_SYSTEM;
import static tech.units.indriya.unit.Units.METRE;

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
    public Collection<Location> addLocations(Collection<VaccineLocation> locations) {
        return locations
                .stream()
                .map(this::addLocation)
                .collect(Collectors.toList());
    }

    @Transactional
    public Location addLocation(VaccineLocation location) {
        this.validateLocation(location);
        final LocationEntity entity = LocationEntity.fromFHIR(location);
        // This shouldn't run on each load, only if things change.


        // Determine if the location already exists, by searching
        final Optional<LocationEntity> maybeExists = this.repo.findOne(hasIdentifier(ORIGINAL_ID_SYSTEM, location.getId()));
        if (maybeExists.isPresent()) {
            final LocationEntity exists = maybeExists.get();
            // Determine if we need to geocode a new point
            // For now, we'll just compare the two addresses and go from there
            if (!entity.getAddress().equals(exists.getAddress())) {
                exists.merge(entity);
                final Point point = this.geocoder.geocodeLocation(entity.getAddress()).block();
                if (point != null) {
                    exists.setCoordinates(point);
                    exists.setH3Index(this.h3.encodePoint(point));
                }
            } else {
                exists.merge(entity);
            }
            return this.repo.save(exists).toFHIR();
        } else {
            // If we have a new location, we need a new point as well, if we don't already have one
            if (entity.getCoordinates() == null) {
                final Point point = this.geocoder.geocodeLocation(entity.getAddress()).block();
                if (point != null) {
                    entity.setCoordinates(point);
                    entity.setH3Index(this.h3.encodePoint(point));
                }
            } else {
                entity.setH3Index(this.h3.encodePoint(entity.getCoordinates()));
            }

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
    public List<Location> findByLocation(NearestQuery query, Pageable pageable) {
        final Iterable<LocationEntity> locationEntities;
        final Point point = query.getPoint();
        if (this.h3.useH3Search()) {
            final List<Long> neighbors = this.h3.findNeighbors(query);
            locationEntities = this.repo.findAll(inH3Indexes(neighbors), pageable);
        } else {
            final double distance = query.getDistanceInMeters();
            locationEntities = this.repo.locationsWithinDistance(point, distance, pageable);
        }

        // Once we have locations, compute the distance between the query point and each location
        return StreamSupport.stream(locationEntities.spliterator(), false)
                .peek(e -> {
                    final Point coords = e.getCoordinates();
                    // Distance is returning in meters
                    final GeodesicData geoDistance = Geodesic.WGS84.Inverse(point.getY(), point.getX(), coords.getY(), coords.getX());
                    final double distanceMeters = geoDistance.s12;
                    Quantity<Length> distanceQuantity = Quantities.getQuantity(distanceMeters, METRE);
//                    // Convert to query units
                    e.setDistanceFromPoint(distanceQuantity.to(query.getDistance().getUnit()));
                })
                .map(LocationEntity::toFHIR)
                .collect(Collectors.toList());
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

    private void validateLocation(VaccineLocation location) {
        final ValidationOptions options = new ValidationOptions();
        options.addProfile(Constants.LOCATION_PROFILE);

        final ValidationResult result = this.validator.validateWithResult(location, options);
        if (!result.isSuccessful()) {
            throw new ValidationFailureException(this.ctx, result.toOperationOutcome());
        }
    }
}
