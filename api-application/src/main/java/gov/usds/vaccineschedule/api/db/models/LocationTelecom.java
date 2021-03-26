package gov.usds.vaccineschedule.api.db.models;

import org.hl7.fhir.r4.model.ContactPoint;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by nickrobison on 3/26/21
 */
@Entity
@Table(name = "location_telecoms")
public class LocationTelecom extends AbstractTelecomEntity<LocationEntity> {

    public LocationTelecom() {
    }

    private LocationTelecom(ContactPoint.ContactPointSystem system, String value) {
        super(system, value);
    }

    public static LocationTelecom fromFHIR(ContactPoint resource) {
        return new LocationTelecom(resource.getSystem(), resource.getValue());
    }
}
