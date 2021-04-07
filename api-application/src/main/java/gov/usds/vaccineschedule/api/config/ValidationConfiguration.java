package gov.usds.vaccineschedule.api.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.FhirValidator;
import org.hl7.fhir.common.hapi.validation.support.CachingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.PrePopulatedValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.r4.conformance.ProfileUtilities;
import org.hl7.fhir.r4.hapi.ctx.HapiWorkerContext;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by nickrobison on 4/6/21
 */
@Configuration
public class ValidationConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(ValidationConfiguration.class);
    private static final String LOCATION_PATTERN = "classpath*:/definitions/*.json";

    @Bean
    FhirValidator buildValidator(FhirContext ctx) {
        final ValidationSupportChain chain = new ValidationSupportChain(new DefaultProfileValidationSupport(ctx),
                new InMemoryTerminologyServerValidationSupport(ctx),
                new CommonCodeSystemsTerminologyService(ctx));

        // Load our custom resource definitions
        logger.debug("Loading custom resource definitions");
        final PrePopulatedValidationSupport customDefinitions = new PrePopulatedValidationSupport(ctx);
        loadDefinitions(ctx, customDefinitions);
        chain.addValidationSupport(customDefinitions);

        final FhirValidator validator = ctx.newValidator();
        final FhirInstanceValidator instanceValidator = new FhirInstanceValidator(new CachingValidationSupport(chain));
        validator.registerValidatorModule(instanceValidator);

        return validator;
    }

    private void loadDefinitions(FhirContext ctx, PrePopulatedValidationSupport validationSupport) {
        final IParser parser = ctx.newJsonParser();

        // Generate a validator to pull the base definitions from.
        final DefaultProfileValidationSupport defaultValidation = new DefaultProfileValidationSupport(ctx);

        final HapiWorkerContext hapiWorkerContext = new HapiWorkerContext(ctx, defaultValidation);

        final ProfileUtilities profileUtilities = new ProfileUtilities(hapiWorkerContext, new ArrayList<>(), null);

        final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader());
        final Resource[] resources;
        try {
            resources = resolver.getResources(LOCATION_PATTERN);
            for (final Resource resource : resources) {
                logger.info("Loading resource definition: {}", resource.getFilename());
                final StructureDefinition definition = parser.parseResource(StructureDefinition.class, resource.getInputStream());
                final StructureDefinition merged = generateSnapshot(ctx, defaultValidation, profileUtilities, definition);
                validationSupport.addStructureDefinition(merged);
            }
        } catch (IOException e) {
            logger.error("Cannot read custom structure definitions", e);
        }
    }

    private StructureDefinition generateSnapshot(FhirContext ctx, DefaultProfileValidationSupport support, ProfileUtilities utils, StructureDefinition diff) {
        final StructureDefinition baseStructure = (StructureDefinition) support.fetchStructureDefinition(diff.getBaseDefinition());
        if (baseStructure != null) {
            utils.generateSnapshot(baseStructure, diff, "", "", "");
        }
        return diff;
    }
}
