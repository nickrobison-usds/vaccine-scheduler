package gov.usds.vaccineschedule.api.helpers;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.inject.Provider;

/**
 * Created by nickrobison on 4/5/21
 * <p>
 * Provides the base URL for the application, which is used to generate bundle links.
 * We're making this a provider so we can call it when we need it, otherwise it gets initialized before the container starts
 */
@Component
public class BaseURLProvider implements Provider<String> {

    private static final String DEFAULT_SERVER = "http://localhost:%s/fhir/";

    private final Environment environment;

    public BaseURLProvider(Environment environment) {
        this.environment = environment;
    }

    @Override
    public String get() {
        // If we have a base url, we should use it. Otherwise, build one using localhost and the current port
        final String baseUrl = environment.getProperty("vs.baseUrl");
        if (baseUrl != null) {
            return baseUrl;
        }
        return String.format(DEFAULT_SERVER, environment.getProperty("local.server.port"));
    }
}
