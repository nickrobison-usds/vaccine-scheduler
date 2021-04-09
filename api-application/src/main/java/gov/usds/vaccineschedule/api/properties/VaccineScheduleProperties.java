package gov.usds.vaccineschedule.api.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

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

    @ConstructorBinding
    public VaccineScheduleProperties(Integer h3Resolution, boolean useH3Search) {
        this.h3Resolution = h3Resolution;
        this.useH3Search = useH3Search;
    }

    public Integer getH3Resolution() {
        return h3Resolution;
    }

    public boolean isUseH3Search() {
        return useH3Search;
    }
}
