package gov.usds.vaccineschedule.api.services;

import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import gov.usds.vaccineschedule.api.db.models.LocationEntity;
import gov.usds.vaccineschedule.api.models.NearestQuery;
import gov.usds.vaccineschedule.api.repositories.LocationRepository;
import gov.usds.vaccineschedule.api.services.geocoder.GeocoderService;
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
import static gov.usds.vaccineschedule.api.repositories.LocationRepository.inState;
import static gov.usds.vaccineschedule.api.services.ServiceHelpers.fromIterable;
import static tech.units.indriya.unit.Units.METRE;

/**
 * Created by nickrobison on 3/26/21
 */
@Service
@Transactional(readOnly = true)
public class LocationService {

    private final LocationRepository repo;
    private final GeocoderService geocoder;

    public LocationService(LocationRepository repo, GeocoderService geocoder) {
        this.repo = repo;
        this.geocoder = geocoder;
    }

    @Transactional
    public Location addLocation(Location location) {
        final LocationEntity entity = LocationEntity.fromFHIR(location);
        return this.repo.save(entity).toFHIR();
    }

    @Transactional
    public Collection<Location> addLocations(Collection<Location> locations) {

        final List<LocationEntity> entities = locations
                .stream()
                .map(LocationEntity::fromFHIR)
                .peek(e -> {
                    final Point point = this.geocoder.geocodeLocation(e.getAddress()).block();
                    e.setCoordinates(point);
                })
                .collect(Collectors.toList());

        return fromIterable(() -> this.repo.saveAll(entities), LocationEntity::toFHIR);
    }

    public Optional<Location> getLocation(IIdType id) {
        final UUID locID = UUID.fromString(id.getIdPart());
        return this.repo.findById(locID).map(LocationEntity::toFHIR);
    }

    public long countLocations(@Nullable TokenParam identifier, @Nullable StringParam city, @Nullable StringParam state) {
        final Optional<Specification<LocationEntity>> optional = buildLocationSpec(identifier, city, state);
        return optional.map(this.repo::count).orElseGet(this.repo::count);
    }

    public List<Location> findLocations(@Nullable TokenParam identifier, @Nullable StringParam city, @Nullable StringParam state, Pageable page) {
        Supplier<Iterable<LocationEntity>> supplier;
        final Optional<Specification<LocationEntity>> optional = buildLocationSpec(identifier, city, state);
        supplier = optional.<Supplier<Iterable<LocationEntity>>>map(specification -> () -> this.repo.findAll(specification, page)).orElseGet(() -> () -> this.repo.findAll(page));
        return StreamSupport.stream(supplier.get().spliterator(), false)
                .map(LocationEntity::toFHIR)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<Location> findByLocation(NearestQuery point, Pageable pageable) {
        final double distance = point.getDistance().to(METRE).getValue().doubleValue();
        return fromIterable(() -> this.repo.locationsWithinDistance(point.getPoint(), distance, pageable), LocationEntity::toFHIR);
    }

    @Transactional
    public long countByLocation(NearestQuery point) {
        final double distance = point.getDistance().to(METRE).getValue().doubleValue();
        return this.repo.countLocationsWithinDistance(point.getPoint(), distance);
    }

    private Optional<Specification<LocationEntity>> buildLocationSpec(@Nullable TokenParam identifier, @Nullable StringParam city, @Nullable StringParam state) {

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
}
