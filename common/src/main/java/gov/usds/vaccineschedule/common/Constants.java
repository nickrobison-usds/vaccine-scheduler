package gov.usds.vaccineschedule.common;

import org.hl7.fhir.r4.model.DateTimeType;
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
    public static final DateTimeFormatter INSTANT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSVV");

    public static String ORIGINAL_ID_SYSTEM = "http://usds.gov/vaccine/source-identifier";
}
