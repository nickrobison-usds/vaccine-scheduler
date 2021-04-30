package gov.usds.vaccineschedule.api.services;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.ValidationFailureException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import gov.usds.vaccineschedule.api.exceptions.MissingUpstreamResource;
import gov.usds.vaccineschedule.api.helpers.LoggingReactorFactory;
import gov.usds.vaccineschedule.api.helpers.MDCConstants;
import gov.usds.vaccineschedule.api.models.SyncRequest;
import gov.usds.vaccineschedule.api.properties.ScheduleSourceConfigProperties;
import gov.usds.vaccineschedule.common.helpers.NDJSONToFHIR;
import gov.usds.vaccineschedule.common.models.PublishResponse;
import gov.usds.vaccineschedule.common.models.VaccineLocation;
import gov.usds.vaccineschedule.common.models.VaccineSlot;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2CodecSupport;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.ContextView;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static gov.usds.vaccineschedule.api.helpers.MDCConstants.IMPORT_COMPLETION;
import static gov.usds.vaccineschedule.api.helpers.MDCConstants.RESOURCE_TYPE;
import static gov.usds.vaccineschedule.api.helpers.MDCConstants.SYNC_SESSION;
import static gov.usds.vaccineschedule.api.helpers.MDCConstants.UPSTREAM_URL;
import static gov.usds.vaccineschedule.common.Constants.FHIR_NDJSON;

/**
 * Created by nickrobison on 3/25/21
 */
@Service
public class SourceFetchService {
    private static final Logger logger = LoggerFactory.getLogger(SourceFetchService.class);

    private static final List<String> resourceOrder = ImmutableList.of("Location", "Schedule", "Slot");
    // Increase the maximum memory buffer size to account for potentially huge payloads
    private static final int MEMORY_SIZE = 1024 * 1024 * 1024;

    private final ScheduleSourceConfigProperties config;
    private final NDJSONToFHIR converter;
    private final Sinks.Many<SyncRequest> processor;
    private final LocationService locationService;
    private final ScheduleService sService;
    private final SlotService slService;
    private final Scheduler dbScheduler;

    private Disposable disposable;

    public SourceFetchService(FhirContext context, ScheduleSourceConfigProperties config, LocationService locationService, ScheduleService sService, SlotService slService) {
        this.config = config;
        this.converter = new NDJSONToFHIR(context.newJsonParser());
        this.slService = slService;
        this.processor = Sinks.many().unicast().onBackpressureBuffer();
        this.locationService = locationService;
        this.sService = sService;

        final ExecutorService executor = Executors.newFixedThreadPool(config.getDbThreadPoolSize());
        this.dbScheduler = Schedulers.fromExecutor(executor);
    }

    @Bean
    public Supplier<Flux<SyncRequest>> supplyStream() {
        return () -> this.processor.asFlux().log();
    }

    @Bean
    public Consumer<Flux<SyncRequest>> receiveStream() {
        return (stream) -> stream.log().subscribe(this::syncUpstream);
    }

    /**
     * Submit the sources to the job queue for processing asynchronously by the workers
     */
    public void refreshSources() {
        final UUID uuid = UUID.randomUUID();
        this.config.getSources().forEach(source -> this.processor.emitNext(new SyncRequest(uuid, source), Sinks.EmitFailureHandler.FAIL_FAST));
    }

    private void processResource(IBaseResource resource) {
        if (resource instanceof VaccineLocation) {
            this.locationService.addLocation((VaccineLocation) resource);
        } else if (resource instanceof Schedule) {
            this.sService.addSchedule((Schedule) resource);
        } else if (resource instanceof VaccineSlot) {
            this.slService.addSlot((VaccineSlot) resource);
        } else {
            logger.error("Cannot handle resource of type: {}", resource);
        }
    }

    private void syncUpstream(SyncRequest source) {
        final LoggingReactorFactory factory = new LoggingReactorFactory(this::baseContextBuilder);

        final WebClient client = buildClient(source.getUrl());
        // Each upstream publisher is independent from the others, so we can process them in parallel
        final Mono<PublishResponse> publishResponseMono = client.get()
                .uri("/$bulk-publish")
                .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)
                .retrieve()
                .bodyToMono(PublishResponse.class)
                .doOnEach(factory.logOnNext(r -> {
                    logger.debug("Receive things from fetcher");
                    logger.debug("Logging should works");
                }));

        this.disposable = buildResourceFetcher(client, publishResponseMono)
                .publishOn(this.dbScheduler)
                .doOnEach(factory.logWithSubscriber(resource -> {
                    logger.debug("Received resource: {}", resource);
                    try {
                        this.processResource(resource);
                        try (MDC.MDCCloseable ignored = MDC.putCloseable(IMPORT_COMPLETION, MDCConstants.ImportStatus.SUCCESS.toString())) {
                            logger.info("{}", resource.getIdElement().getValue());
                        }
                    } catch (ValidationFailureException e) {
                        try (MDC.MDCCloseable ignored = MDC.putCloseable(IMPORT_COMPLETION, MDCConstants.ImportStatus.VALIDATION.toString())) {
                            logger.error("Resource failed validation. continuing", e);
                        }
                    } catch (MissingUpstreamResource e) {
                        try (MDC.MDCCloseable ignored = MDC.putCloseable(IMPORT_COMPLETION, MDCConstants.ImportStatus.SYSTEM.toString())) {
                            logger.error("Unable to find required upstream resource. continuing", e);
                        }
                    }
                }, (error) -> {
                    throw new RuntimeException(error);
                }, () -> logger.info("Finished refreshing data for {}", source.getSyncSessionID())))
                .contextWrite(ctx -> ctx.put(SYNC_SESSION, source.getSyncSessionID())
                        .put(UPSTREAM_URL, source.getUrl()))
                .subscribe();
    }

    private Flux<IBaseResource> handleResource(WebClient client, String resourceType, PublishResponse response) {
        final LoggingReactorFactory factory = new LoggingReactorFactory((ctx) -> {
            final Map<String, String> base = new java.util.HashMap<>(baseContextBuilder(ctx));
            base.put(RESOURCE_TYPE, ctx.get(RESOURCE_TYPE));
            return base;
        });
        return Flux.fromIterable(response.getOutput())
                .doOnEach(factory.logOnNext(o -> {
                    logger.debug("Fetching resource of type: {} from: {}", o.getType(), o.getUrl());
                }))
                .flatMap(output -> client.get().uri(output.getUrl()).accept(MediaType.parseMediaType(FHIR_NDJSON)).retrieve().bodyToMono(DataBuffer.class))
                .flatMap(body -> Flux.fromIterable(converter.inputStreamToResource(body.asInputStream(true))))
                .contextWrite(ctx -> ctx.put(RESOURCE_TYPE, resourceType));
    }

    Flux<IBaseResource> buildResourceFetcher(WebClient client, Mono<PublishResponse> provider) {

        return provider
                .flatMapMany(response -> Flux.fromArray(resourceOrder.toArray(new String[]{}))
                        // Process the responses in the following order: Location -> Schedule -> Slot
                        // It seems to me that we should be able to use a flatMapSequential, but this is the safer, albeit more pessimistic solution.
                        .concatMap(resourceType -> handleResource(client, resourceType, response)))
                .onErrorContinue((error) -> error instanceof WebClientException,
                        (throwable, o) -> { // If we throw an exception
                            logger.error("Cannot process resource: {}", o, throwable);
                        });
    }

    private WebClient buildClient(String baseURL) {
        return WebClient.builder()
                .baseUrl(baseURL)
                // Configure Jackson to handle text/plain content types as well, such as what we get from Github
                .codecs(configurer -> {
                    // An API may return JSON with content type text/plain, so need to register a custom
                    // decoder to deserialize this response via Jackson
                    // Get existing decoder's ObjectMapper if available, or create new one
                    ObjectMapper objectMapper = configurer.getReaders().stream()
                            .filter(reader -> reader instanceof Jackson2JsonDecoder)
                            .map(reader -> (Jackson2JsonDecoder) reader)
                            .map(Jackson2CodecSupport::getObjectMapper)
                            .findFirst()
                            .orElseGet(() -> Jackson2ObjectMapperBuilder.json().build());

                    Jackson2JsonDecoder decoder = new Jackson2JsonDecoder(objectMapper, MediaType.TEXT_PLAIN);
                    // Bump up the maximum data size to 1 MB
                    decoder.setMaxInMemorySize(MEMORY_SIZE);
                    configurer.defaultCodecs().maxInMemorySize(MEMORY_SIZE);
                    configurer.customCodecs().registerWithDefaultConfig(decoder);
                })
                .build();
    }

    private Map<String, String> baseContextBuilder(ContextView ctx) {
        return Map.of(SYNC_SESSION, ctx.get(SYNC_SESSION).toString(),
                UPSTREAM_URL, ctx.get(UPSTREAM_URL));
    }
}
