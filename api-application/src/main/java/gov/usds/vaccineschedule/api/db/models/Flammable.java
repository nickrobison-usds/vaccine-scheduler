package gov.usds.vaccineschedule.api.db.models;

import org.hl7.fhir.instance.model.api.IBaseResource;

@FunctionalInterface
public interface Flammable<T extends IBaseResource> {

    T toFHIR();
}
