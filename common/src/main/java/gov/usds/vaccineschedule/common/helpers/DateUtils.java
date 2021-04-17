package gov.usds.vaccineschedule.common.helpers;

import java.time.OffsetDateTime;
import java.util.Date;

/**
 * Created by nickrobison on 4/16/21
 */
public class DateUtils {

    public static Date offsetDateTimeToDate(OffsetDateTime date) {
        return new Date(date.toInstant().toEpochMilli());
    }
}
