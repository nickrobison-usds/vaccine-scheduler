package gov.usds.vaccineschedule.api.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationOptions;
import ca.uhn.fhir.validation.ValidationResult;
import gov.usds.vaccineschedule.api.BaseApplicationTest;
import gov.usds.vaccineschedule.common.helpers.NDJSONToFHIR;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Schedule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by nickrobison on 4/6/21
 */
public class TestValidations extends BaseApplicationTest {

    @Autowired
    FhirValidator validator;
    @Autowired
    FhirContext ctx;

    @Test
    void testFailingValidation() {
        // Test with a basic location
        final Location location = new Location();
        location.setId("test-location");

        ValidationOptions options = new ValidationOptions();
        options.addProfile("http://fhir-registry.smarthealthit.org/StructureDefinition/vaccine-location");

        final ValidationResult validationResult = validator.validateWithResult(location, options);
        assertAll(() -> assertFalse(validationResult.isSuccessful(), "Validation should have failed"),
                () -> assertEquals(3, validationResult.getMessages().size(), "Should have specific failures"));
        assertFalse(validationResult.isSuccessful());

        // Test with a basic schedule
        final Schedule schedule = new Schedule();
        schedule.setId("test-schedule");
        schedule.setActor(List.of(new Reference("test-location")));
        schedule.setServiceType(List.of(new CodeableConcept(new Coding("http://terminology.hl7.org/CodeSystem/service-type", "57", ""))));
        options = new ValidationOptions();
        options.addProfile("http://fhir-registry.smarthealthit.org/StructureDefinition/vaccine-schedule");
        final ValidationResult vR2 = validator.validateWithResult(schedule, options);

        assertAll(() -> assertFalse(vR2.isSuccessful()),
                () -> assertEquals(1, vR2.getMessages().size()));
    }

    @Test
    void testPassingValidation() throws IOException {
        final IParser parser = ctx.newJsonParser();
        // Load a single schedule resource
        final NDJSONToFHIR converter = new NDJSONToFHIR(parser);
        try (InputStream stream = FhirValidator.class.getClassLoader().getResourceAsStream("example-schedules.ndjson")) {
            final Schedule schedule = converter.inputStreamToTypedResource(Schedule.class, stream).get(0);

            final ValidationOptions options = new ValidationOptions();
            options.addProfile("http://fhir-registry.smarthealthit.org/StructureDefinition/vaccine-schedule");

            final ValidationResult validationResult = validator.validateWithResult(schedule, options);
            assertTrue(validationResult.isSuccessful());
        }
    }
}
