package gov.usds.vaccineschedule.api.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * Created by nickrobison on 4/7/21
 */
@ConfigurationProperties(prefix = "rollbar")
public class RollbarConfigurationProperties {

    private boolean enabled;
    private String accessToken;
    private String branch;
    private String codeVersion;
    private String environment;

    @ConstructorBinding
    public RollbarConfigurationProperties(boolean enabled, String accessToken, String branch, String codeVersion, String environment) {
        this.enabled = enabled;
        this.accessToken = accessToken;
        this.branch = branch;
        this.codeVersion = codeVersion;
        this.environment = environment;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getBranch() {
        return branch;
    }

    public String getCodeVersion() {
        return codeVersion;
    }

    public String getEnvironment() {
        return environment;
    }
}
