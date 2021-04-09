package gov.usds.vaccineschedule.common.models;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.Extension;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Quantity;

/**
 * Created by nickrobison on 4/9/21
 */
@ResourceDef(name = "Location", profile = "http://fhir-registry.smarthealthit.org/StructureDefinition/vaccine-location")
public class VaccineLocation extends Location {
    private static final long serialVersionUID = 42L;

    @Extension(url = "http://hl7.org/fhir/StructureDefinition/location-distance", isModifier = false, definedLocally = false)
    @Description(shortDefinition = "A calculated distance between the resource and a provided location")
    @Child(name = "locationDistance")
    private Quantity locationDistance;

    public Quantity getLocationDistance() {
        if (locationDistance == null) {
            locationDistance = new Quantity();
        }
        return locationDistance;
    }

    public void setLocationDistance(Quantity quantity) {
        this.locationDistance = quantity;
    }


}
