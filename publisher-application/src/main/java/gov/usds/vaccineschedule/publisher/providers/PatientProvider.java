package gov.usds.vaccineschedule.publisher.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jaxrs.server.AbstractJaxRsResourceProvider;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by nickrobison on 3/25/21
 */
@Component
public class PatientProvider extends AbstractJaxRsResourceProvider<Patient> {

    private static Long counter = 1L;

    private static final ConcurrentHashMap<String, Patient> patients = new ConcurrentHashMap<>();

    static {
        patients.put(String.valueOf(counter), createPatient("Van Houte"));
        patients.put(String.valueOf(counter), createPatient("Agnew"));
        for (int i = 0; i < 20; i++) {
            patients.put(String.valueOf(counter), createPatient("Random Patient " + counter));
        }
    }

    public PatientProvider(FhirContext ctx) {
        super(ctx);
    }

    @Override
    public Class<Patient> getResourceType() {
        return Patient.class;
    }

    @Search
    public Collection<Patient> patientSearch() {
        return patients.values();
    }

    @Create
    public MethodOutcome createPatient(@ResourceParam Patient patient) {
        patient.setId(createId(counter, 1L));
        patients.put(String.valueOf(counter), patient);

        return new MethodOutcome(patient.getIdElement());
    }

    @Read
    public Patient find(@IdParam IdType id) {
        if (patients.containsKey(id.getIdPart())) {
            return patients.get(id.getIdPart());
        } else {
            throw new ResourceNotFoundException(id);
        }
    }

    private static IdType createId(final Long id, final Long theVersionId) {
        return new IdType("Patient", "" + id, "" + theVersionId);
    }

    private static Patient createPatient(final String name) {
        final Patient patient = new Patient();
        patient.getName().add(new HumanName().setFamily(name));
        patient.setId(createId(counter, 1L));
        counter++;
        return patient;
    }
}
