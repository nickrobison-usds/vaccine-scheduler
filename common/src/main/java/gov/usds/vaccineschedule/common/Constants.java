package gov.usds.vaccineschedule.common;

import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.InstantType;

import java.time.format.DateTimeFormatter;

/**
 * Created by nickrobison on 4/6/21
 */
public class Constants {

    public static final String FHIR_NDJSON = "application/fhir+ndjson";
    /**
     * {@link DateTimeFormatter} which outputs in a format parsable by {@link DateTimeType}.
     * See: https://www.hl7.org/fhir/datatypes.html#dateTime
     */
    public static final DateTimeFormatter FHIR_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx");


    /**
     * {@link DateTimeFormatter} which outputs in a format parsable by {@link InstantType}.
     * See: https://www.hl7.org/fhir/datatypes.html#instant
     */
//    public static final DateTimeFormatter INSTANT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSxxx");

    // Profiles
    public static final DateTimeFormatter INSTANT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSVV");
    public static final String LOCATION_PROFILE = "http://fhir-registry.smarthealthit.org/StructureDefinition/vaccine-location";
    public static final String SLOT_PROFILE = "http://fhir-registry.smarthealthit.org/StructureDefinition/vaccine-slot";
    public static final String SCHEDULE_PROFILE = "http://fhir-registry.smarthealthit.org/StructureDefinition/vaccine-schedule";

    // Extensions
    public static String ORIGINAL_ID_SYSTEM = "http://usds.gov/vaccine/source-identifier";
    public static String CURRENT_AS_OF = "http://usds.gov/vaccine/currentAsOf";

    public static final String HL7_SYSTEM = "http://terminology.hl7.org/CodeSystem/service-type";
    public static final String SMART_SYSTEM = "http://fhir-registry.smarthealthit.org/CodeSystem/service-type";
    // Schedule Identifiers
    public static Identifier HL7_IDENTIFIER = new Identifier().setSystem(HL7_SYSTEM).setValue("57");

    public static Identifier SMART_IDENTIFIER = new Identifier().setSystem(SMART_SYSTEM).setValue("covid19-immunization");
}
