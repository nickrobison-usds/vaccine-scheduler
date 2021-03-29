package gov.usds.vaccineschedule.api.db.models;

import org.hl7.fhir.r4.model.Identifier;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by nickrobison on 3/26/21
 */
@Entity
@Table(name = "schedule_identifiers")
public class ScheduleIdentifier extends AbstractIdentifierEntity<ScheduleEntity> {

    public ScheduleIdentifier() {
        // Hibernate required
    }

    public ScheduleIdentifier(String system, String value) {
        super(system, value);
    }

    public static ScheduleIdentifier fromFHIR(Identifier resource) {
        return new ScheduleIdentifier(resource.getSystem(), resource.getValue());
    }
}
