package gov.usds.vaccineschedule.api.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.context.annotation.Bean;

/**
 * Created by nickrobison on 4/8/21
 */
@ConfigurationProperties(prefix = "vs")
public class VaccineScheduleProperties {

    /**
     * Default resolution to use when generating H3 ID
     */
    private final Integer h3Resolution;
    /**
     * Whether or not to use H3 for doing the distance searches
     * This *should* be faster, but less accurate
     */
    private final boolean useH3Search;

    private final ScheduleSourceConfigProperties scheduleSource;

    @ConstructorBinding
    public VaccineScheduleProperties(Integer h3Resolution, boolean useH3Search, ScheduleSourceConfigProperties scheduleSource) {
        this.h3Resolution = h3Resolution;
        this.useH3Search = useH3Search;
        this.scheduleSource = scheduleSource;
    }

    public Integer getH3Resolution() {
        return h3Resolution;
    }

    public boolean isUseH3Search() {
        return useH3Search;
    }

    @Bean
    public ScheduleSourceConfigProperties getScheduleSource() {
        return scheduleSource;
    }
}
