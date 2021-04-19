package gov.usds.vaccineschedule.wapublisher.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import gov.usds.vaccineschedule.wapublisher.models.BundledAvailability;
import gov.usds.vaccineschedule.wapublisher.services.UpstreamService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.cache.CacheMono;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import javax.ws.rs.NotFoundException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static gov.usds.vaccineschedule.common.Constants.FHIR_NDJSON;

/**
 * Created by nickrobison on 4/16/21
 */
@RestController
@RequestMapping("/data")
public class DataProvider {
    private final IParser parser;

    private final UpstreamService service;
    private final Cache cache;

    public DataProvider(FhirContext ctx, UpstreamService service, CacheManager cacheManager) {
        this.parser = ctx.newJsonParser();
        this.service = service;
        this.cache = Objects.requireNonNull(cacheManager.getCache("availability"));
    }

    @GetMapping(value = "/{id}.ndjson", produces = FHIR_NDJSON)
    public Mono<String> getFile(@PathVariable String id) {

        final Function<BundledAvailability, Flux<IBaseResource>> supplier;
        switch (id) {
            case "slot":
                supplier = (bundle) -> Flux.fromIterable(bundle.getSlots());
                break;
            case "location":
                supplier = (bundle) -> Flux.just(bundle.getLocation());
                break;
            case "schedule":
                supplier = (bundle) -> Flux.fromIterable(bundle.getSchedules());
                break;
            default:
                throw new NotFoundException(String.format("Unknown resource type: %s", id));
        }

        return CacheMono.lookup(this::readFromCache, id)
                .onCacheMissResume(() -> fetchFromService(supplier))
                .andWriteWith(this::writeToCache);
    }

    private Mono<String> fetchFromService(Function<BundledAvailability, Flux<IBaseResource>> supplier) {
        return service.getUpstreamAvailability()
                .flatMap(supplier)
                .map(this.parser::encodeResourceToString)
                .collectList()
                .map(resources -> String.join("\n", resources));
    }

    @SuppressWarnings("unchecked")
    private Mono<Signal<? extends String>> readFromCache(String key) {
        final Optional<Signal<? extends String>> maybeValue = Optional.ofNullable(this.cache.get(key)).map(val -> (Signal<String>) val.get());
        return Mono.justOrEmpty(maybeValue);
    }

    private Mono<Void> writeToCache(String key, Signal<? extends String> value) {
        return Mono.fromRunnable(() -> this.cache.put(key, value));
    }
}
