package gov.usds.vaccineschedule.api.db.models;

import org.hl7.fhir.r4.model.Identifier;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by nickrobison on 3/29/21
 */
@Entity
@Table(name = "slot_identifiers")
public class SlotIdentifier extends AbstractIdentifierEntity<SlotEntity> {

    public SlotIdentifier() {
        // Hibernate required
    }

    public SlotIdentifier(String system, String value) {
        super(system, value);
    }

    public static SlotIdentifier fromFHIR(Identifier resource) {
        return new SlotIdentifier(resource.getSystem(), resource.getValue());
    }
}
