package gov.usds.vaccineschedule.wapublisher.config;

import ca.uhn.fhir.context.FhirContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by nickrobison on 4/16/21
 */
@Configuration
public class FhirConfig {
    
    @Bean
    public FhirContext provideCtx() {
        return FhirContext.forR4();
    }
}
