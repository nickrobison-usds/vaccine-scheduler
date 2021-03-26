package gov.usds.vaccineschedule.api.services;

import gov.usds.vaccineschedule.api.db.models.LocationEntity;
import gov.usds.vaccineschedule.api.repositories.LocationRepository;
import org.hl7.fhir.r4.model.Location;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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


    public Collection<Location> getLocations() {
        return StreamSupport
                .stream(this.repo.findAll().spliterator(), false)
                .map(LocationEntity::toFHIR)
                .collect(Collectors.toList());
    }
}
