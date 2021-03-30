package gov.usds.vaccineschedule.api.services;

import gov.usds.vaccineschedule.api.db.models.LocationEntity;
import gov.usds.vaccineschedule.api.repositories.LocationRepository;
import org.hl7.fhir.r4.model.Location;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static gov.usds.vaccineschedule.api.services.ServiceHelpers.fromIterable;

/**
 * Created by nickrobison on 3/26/21
 */
@Service
@Transactional(readOnly = true)
public class LocationService {

    private final LocationRepository repo;

    public LocationService(LocationRepository repo) {
        this.repo = repo;
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
                .collect(Collectors.toList());

        return fromIterable(() -> this.repo.saveAll(entities), LocationEntity::toFHIR);
    }


    public Collection<Location> getLocations() {
        return fromIterable(this.repo::findAll, LocationEntity::toFHIR);
    }


}
