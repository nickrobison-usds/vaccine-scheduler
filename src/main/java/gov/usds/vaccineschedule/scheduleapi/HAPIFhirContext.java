package gov.usds.vaccineschedule.scheduleapi;

import ca.uhn.fhir.context.FhirContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by nickrobison on 3/25/21
 */
@Configuration
public class HAPIFhirContext {

    @Bean
    FhirContext getContext() {
        return FhirContext.forR4();
    }
}
