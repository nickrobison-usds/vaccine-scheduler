package gov.usds.vaccineschedule.api.helpers;

/**
 * Created by nickrobison on 4/29/21
 */
public class MDCConstants {
    public static final String SYNC_SESSION = "syncSession";
    public static final String UPSTREAM_URL = "upstream";
    public static final String RESOURCE_TYPE = "resourceType";
    public static final String RESOURCE_ID = "resourceID";
    public static final String IMPORT_COMPLETION = "importCompletion";

    public enum ImportStatus {
        SUCCESS,
        VALIDATION,
        SYSTEM
    }
}
