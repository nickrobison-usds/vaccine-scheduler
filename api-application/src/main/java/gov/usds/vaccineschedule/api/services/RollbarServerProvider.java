package gov.usds.vaccineschedule.api.services;

import com.rollbar.api.payload.data.Server;
import com.rollbar.notifier.provider.Provider;
import gov.usds.vaccineschedule.api.properties.RollbarConfigurationProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Created by nickrobison on 4/7/21
 */
@Component
@Lazy
public class RollbarServerProvider implements Provider<Server> {

    private final Environment env;
    private final RollbarConfigurationProperties config;

    public RollbarServerProvider(Environment env, RollbarConfigurationProperties config) {
        this.env = env;
        this.config = config;
    }

    @Override
    public Server provide() {
        return new Server.Builder()
                .codeVersion(config.getCodeVersion())
                .branch(config.getBranch())
                .host(env.getProperty("spring.application.name"))
                .root("gov.usds.vaccineschedule.api")
                .build();
    }
}
