package gov.usds.vaccineschedule.api.services;

import ca.uhn.fhir.context.FhirContext;
import cov.usds.vaccineschedule.common.models.PublishResponse;
import gov.usds.vaccineschedule.api.config.ScheduleSourceConfig;
import gov.usds.vaccineschedule.api.db.models.LocationEntity;
import gov.usds.vaccineschedule.api.repositories.LocationRepository;
import gov.usds.vaccineschedule.common.helpers.NDJSONToFHIR;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Schedule;
import org.hl7.fhir.r4.model.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Sinks;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by nickrobison on 3/25/21
 */
@Service
public class SourceFetchService {
    private static final Logger logger = LoggerFactory.getLogger(SourceFetchService.class);

    private final ScheduleSourceConfig config;
    private final NDJSONToFHIR converter;
    private final Sinks.Many<String> processor;
    private final LocationRepository locationRepo;
    private final ScheduleService sService;
    private final SlotService slService;

    private Disposable disposable;

    public SourceFetchService(FhirContext context, ScheduleSourceConfig config, LocationRepository locationRepository, ScheduleService sService, SlotService slService) {
        this.config = config;
        this.converter = new NDJSONToFHIR(context.newJsonParser());
        this.slService = slService;
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
     * Submit the sources to the job queue for processing, asynchronously by the workers
     */
    public void refreshSources() {
        this.config.getSources().forEach(source -> this.processor.emitNext(source, Sinks.EmitFailureHandler.FAIL_FAST));
    }

    private void handleRefresh(String source) {
        // Each upstream publisher is independent from the others, so we can process them in parallel
        final WebClient client = WebClient.create(source);
        client.get()
                .uri("/$bulk-publish")
                .retrieve()
                .bodyToMono(PublishResponse.class)
                .flatMapMany(response -> Flux.fromIterable(response.getOutput())
                        .groupBy(PublishResponse.OutputEntry::getType)
                        // Because the bulk spec doesn't provide any mechanism for hinting at order. we have to process things in this order:
                        // Location -> Schedule -> Slot, since each depends on the previous resource
                        // We can hack this by grouping and sorting, since they happen to be in alphabetical order
                        .sort(Comparator.comparing(GroupedFlux::key))
                        .flatMap(group -> group
                                .flatMap(url -> client.get().uri("data" + url.getUrl()).retrieve().bodyToMono(DataBuffer.class))
                                .flatMap(body -> Flux.fromIterable(converter.inputStreamToResource(body.asInputStream(true))))))
                .onErrorContinue((error) -> error instanceof WebClientException,
                        (throwable, o) -> { // If we throw an exception
                            logger.error("Cannot process resource: {}", o, throwable);
                        })
                .subscribe(resource -> {
                    logger.info("Received resource: {}", resource);
                    this.processResource(resource);
                }, (error) -> {
                    throw new RuntimeException(error);
                });
    }

    private void processResource(IBaseResource resource) {
        if (resource instanceof Location) {
            final LocationEntity entity = LocationEntity.fromFHIR((Location) resource);
            locationRepo.save(entity);
        } else if (resource instanceof Schedule) {
            this.sService.addSchedule((Schedule) resource);
        } else if (resource instanceof Slot) {
            this.slService.addSlot((Slot) resource);
        } else {
            logger.error("Cannot handle resource of type: {}", resource);
        }
    }
}
