package gov.usds.vaccineschedule.api.config;

import com.rollbar.notifier.Rollbar;
import com.rollbar.notifier.config.Config;
import com.rollbar.spring.webmvc.RollbarSpringConfigBuilder;
import gov.usds.vaccineschedule.api.properties.RollbarConfigProperties;
import gov.usds.vaccineschedule.api.services.RollbarServerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

/**
 * Created by nickrobison on 4/7/21
 */
@Component
@ConditionalOnProperty(value = "rollbar.enabled", havingValue = "true")
@ComponentScan("com.rollbar.spring")
public class RollbarConfig {

    private static final Logger logger = LoggerFactory.getLogger(RollbarConfig.class);

    @Bean
    public Rollbar rollbar(RollbarConfigProperties properties, RollbarServerProvider provider) {
        logger.debug("Rollbar properties: {}", properties);

        final Config builder = RollbarSpringConfigBuilder
                .withAccessToken(properties.getAccessToken())
                .environment(properties.getEnvironment())
                .server(provider).build();
        return new Rollbar(builder);
    }
}
