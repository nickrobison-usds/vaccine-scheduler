package gov.usds.vaccineschedule.api.services;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationFailureException;
import ca.uhn.fhir.validation.ValidationOptions;
import ca.uhn.fhir.validation.ValidationResult;
import com.google.common.collect.ImmutableList;
import gov.usds.vaccineschedule.api.config.ScheduleSourceConfig;
import gov.usds.vaccineschedule.api.db.models.LocationEntity;
import gov.usds.vaccineschedule.api.repositories.LocationRepository;
import gov.usds.vaccineschedule.common.helpers.NDJSONToFHIR;
import gov.usds.vaccineschedule.common.models.PublishResponse;
import gov.usds.vaccineschedule.common.models.VaccineSlot;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static gov.usds.vaccineschedule.common.Constants.FHIR_NDJSON;

/**
 * Created by nickrobison on 3/25/21
 */
@Service
public class SourceFetchService {
    private static final Logger logger = LoggerFactory.getLogger(SourceFetchService.class);

    private static final Map<String, String> profileMap = buildProfileMap();
    private static final List<String> resourceOrder = ImmutableList.of("Location", "Schedule", "Slot");

    private final FhirContext ctx;
    private final ScheduleSourceConfig config;
    private final NDJSONToFHIR converter;
    private final Sinks.Many<String> processor;
    private final LocationRepository locationRepo;
    private final ScheduleService sService;
    private final SlotService slService;
    private final FhirValidator validator;

    private Disposable disposable;

    public SourceFetchService(FhirContext context, ScheduleSourceConfig config, LocationRepository locationRepository, ScheduleService sService, SlotService slService, FhirValidator validator) {
        this.ctx = context;
        this.config = config;
        this.converter = new NDJSONToFHIR(context.newJsonParser());
        this.slService = slService;
        this.validator = validator;
        this.processor = Sinks.many().unicast().onBackpressureBuffer();
        this.locationRepo = locationRepository;
        this.sService = sService;
    }

    @Bean
    public Supplier<Flux<String>> supplyStream() {
        return () -> this.processor.asFlux().log();
    }

    @Bean
    public Consumer<Flux<String>> receiveStream() {
        return (stream) -> stream.log().subscribe(this::handleRefresh);
    }

    /**
     * Submit the sources to the job queue for processing asynchronously by the workers
     */
    public void refreshSources() {
        this.config.getSources().forEach(source -> this.processor.emitNext(source, Sinks.EmitFailureHandler.FAIL_FAST));
    }

    private void processResource(IBaseResource resource) {
        if (resource instanceof Location) {
            final LocationEntity entity = LocationEntity.fromFHIR((Location) resource);
            locationRepo.save(entity);
        } else if (resource instanceof Schedule) {
            this.sService.addSchedule((Schedule) resource);
        } else if (resource instanceof VaccineSlot) {
            this.slService.addSlot((VaccineSlot) resource);
        } else {
            logger.error("Cannot handle resource of type: {}", resource);
        }
    }

    private void handleRefresh(String source) {
        // Each upstream publisher is independent from the others, so we can process them in parallel
        final WebClient client = WebClient.create(source);
        final Mono<PublishResponse> publishResponseMono = client.get()
                .uri("/$bulk-publish")
                .retrieve()
                .bodyToMono(PublishResponse.class);

        this.disposable = buildResourceFetcher(client, publishResponseMono)
                .doOnComplete(() -> logger.info("Finished refreshing data for {}", source))
                .subscribe(resource -> {
                    logger.debug("Received resource: {}", resource);
                    this.processResource(resource);
                }, (error) -> {
                    throw new RuntimeException(error);
                });
    }

    private Flux<IBaseResource> handleResource(WebClient client, String resourceType, PublishResponse response) {
        return Flux.fromIterable(response.getOutput())
                .filter(o -> o.getType().equals(resourceType))
                .doOnNext(o -> logger.debug("Fetching resource of type: {} from: {}", o.getType(), o.getUrl()))
                .flatMap(output -> client.get().uri(output.getUrl()).accept(MediaType.parseMediaType(FHIR_NDJSON)).retrieve().bodyToMono(DataBuffer.class))
                .flatMap(body -> Flux.fromIterable(converter.inputStreamToResource(body.asInputStream(true))))
                .doOnNext(this::validateResource);
    }

    private void validateResource(IBaseResource resource) {
        final String resourceName = resource.getClass().getSimpleName();
        final String profile = profileMap.get(resourceName);
        logger.debug("Validating {} resource against profile: {}", resourceName, profile);
        final ValidationOptions options = new ValidationOptions();
        options.addProfile(profile);
        final ValidationResult result = this.validator.validateWithResult(resource, options);
        if (!result.isSuccessful()) {
            throw new ValidationFailureException(this.ctx, result.toOperationOutcome());
        }
    }


    Flux<IBaseResource> buildResourceFetcher(WebClient client, Mono<PublishResponse> provider) {
        return provider
                .flatMapMany(response -> Flux.fromIterable(resourceOrder)
                        // Process the responses in the following order: Location -> Schedule -> Slot
                        .flatMapSequential(resourceType -> handleResource(client, resourceType, response)))
                .onErrorContinue((error) -> error instanceof WebClientException,
                        (throwable, o) -> { // If we throw an exception
                            logger.error("Cannot process resource: {}", o, throwable);
                        });
    }

    private static Map<String, String> buildProfileMap() {
        return Map.of("Location", "http://fhir-registry.smarthealthit.org/StructureDefinition/vaccine-location",
                "Schedule", "http://fhir-registry.smarthealthit.org/StructureDefinition/vaccine-schedule",
                "VaccineSlot", "http://fhir-registry.smarthealthit.org/StructureDefinition/vaccine-slot");
    }
}
