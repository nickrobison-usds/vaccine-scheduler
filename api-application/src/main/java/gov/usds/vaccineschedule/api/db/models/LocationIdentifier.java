package gov.usds.vaccineschedule.api.db.models;

import org.hl7.fhir.r4.model.Identifier;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by nickrobison on 3/26/21
 */
@Entity
@Table(name = "location_identifiers")
public class LocationIdentifier extends AbstractIdentifierEntity<LocationEntity> {

    public LocationIdentifier(String system, String value) {
        super(system, value);
    }

    public LocationIdentifier() {
        // Hibernate required
    }


    public static LocationIdentifier fromFHIR(Identifier resource) {
        return new LocationIdentifier(resource.getSystem(), resource.getValue());
    }
}
