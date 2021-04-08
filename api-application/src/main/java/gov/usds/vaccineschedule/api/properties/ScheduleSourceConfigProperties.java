package gov.usds.vaccineschedule.api.properties;

import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.List;
import java.util.TimeZone;

/**
 * Created by nickrobison on 3/25/21
 */
public class ScheduleSourceConfigProperties {

    private final boolean scheduleEnabled;

    private final List<String> sources;

    /**
     * A list of <a href=
     * "https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/support/CronExpression.html">cron
     * expressions</a> to use to schedule the uploader job (most likely a single-element list in all
     * cases, but flexibility available if we want it).
     */
    private final List<String> refreshSchedule;
    /**
     * The time zone for the cron expressions in the schedule (default: GMT)
     */
    private final TimeZone scheduleTimezone;

    private final Integer dbThreadPoolSize;

    @ConstructorBinding
    public ScheduleSourceConfigProperties(boolean scheduleEnabled, List<String> sources, List<String> refreshSchedule, TimeZone scheduleTimezone, Integer dbThreadPoolSize) {
        this.scheduleEnabled = scheduleEnabled;
        this.sources = sources;
        this.refreshSchedule = refreshSchedule;
        this.scheduleTimezone = scheduleTimezone;
        this.dbThreadPoolSize = dbThreadPoolSize;
    }

    public boolean getScheduleEnabled() {
        return scheduleEnabled;
    }

    public List<String> getSources() {
        return sources;
    }

    public List<String> getRefreshSchedule() {
        return refreshSchedule;
    }

    public TimeZone getScheduleTimezone() {
        return scheduleTimezone;
    }

    public Integer getDbThreadPoolSize() {
        return dbThreadPoolSize;
    }
}
