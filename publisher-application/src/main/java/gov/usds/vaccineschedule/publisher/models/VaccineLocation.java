package gov.usds.vaccineschedule.publisher.models;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import org.hl7.fhir.r4.model.Location;

/**
 * Created by nickrobison on 3/25/21
 */
@ResourceDef(name = "Location", profile = "http://fhir-registry.smarthealthit.org/StructureDefinition/vaccine-location")
public class VaccineLocation extends Location {
}
