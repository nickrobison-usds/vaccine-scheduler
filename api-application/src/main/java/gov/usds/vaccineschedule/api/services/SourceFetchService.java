package gov.usds.vaccineschedule.api.services;

import ca.uhn.fhir.context.FhirContext;
import cov.usds.vaccineschedule.common.models.PublishResponse;
import gov.usds.vaccineschedule.api.config.ScheduleSourceConfig;
import gov.usds.vaccineschedule.common.helpers.NDJSONToFHIR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

/**
 * Created by nickrobison on 3/25/21
 */
@Service
public class SourceFetchService {
    private static final Logger logger = LoggerFactory.getLogger(SourceFetchService.class);

    private final RestTemplate template;
    private final ScheduleSourceConfig config;
    private final NDJSONToFHIR converter;

    private Disposable disposable;

    public SourceFetchService(FhirContext context, RestTemplate restTemplate, ScheduleSourceConfig config) {
        this.config = config;
        this.template = restTemplate;
        this.converter = new NDJSONToFHIR(context.newJsonParser());
    }

    public void refreshSources() {

        this.disposable = Flux.fromIterable(config.getSources())
                .flatMap(source -> {
                    final WebClient client = WebClient.create(source);
                    return client.get()
                            .uri("/$bulk-publish")
                            .retrieve()
                            .bodyToMono(PublishResponse.class)
                            .flatMapMany(response -> Flux.fromIterable(response.getOutput())
                                    .map(PublishResponse.OutputEntry::getUrl)
                                    .flatMap(url -> client.get().uri("data" + url).retrieve().bodyToMono(DataBuffer.class))
                                    .flatMap(body -> Flux.fromIterable(converter.inputStreamToResource(body.asInputStream(true))))
                            );
                })
                .onErrorContinue((error) -> error instanceof WebClientException,
                        (throwable, o) -> { // If we throw an exception
                            logger.error("Cannot process resource: {}", o, throwable);
                        })
                .subscribe(resource -> logger.info("Received resource: {}", resource), (error) -> {
                    throw new RuntimeException(error);
                });
    }
}
