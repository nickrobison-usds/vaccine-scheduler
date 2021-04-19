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
import reactor.cache.CacheFlux;
import reactor.cache.CacheMono;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import javax.ws.rs.NotFoundException;
import java.util.List;
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
    private final Cache availCache;
    private final Cache upstreamCache;

    public DataProvider(FhirContext ctx, UpstreamService service, CacheManager cacheManager) {
        this.parser = ctx.newJsonParser();
        this.service = service;
        this.availCache = Objects.requireNonNull(cacheManager.getCache("availability"));
        this.upstreamCache = Objects.requireNonNull(cacheManager.getCache("upstream"));
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


        return CacheMono.lookup(this::readFromAvailCache, id)
                .onCacheMissResume(() -> fetchFromService(supplier))
                .andWriteWith(this::writeToAvailCache);
    }

    private Mono<String> fetchFromService(Function<BundledAvailability, Flux<IBaseResource>> supplier) {
        return this.fetchFromUpstream("WA")
                .flatMap(supplier)
                .map(this.parser::encodeResourceToString)
                .collectList()
                .map(resources -> String.join("\n", resources));
    }

    private Flux<BundledAvailability> fetchFromUpstream(String state) {
        return CacheFlux.lookup(this::readFromUpstreamCache, state)
                .onCacheMissResume(this.service::getUpstreamAvailability)
                .andWriteWith(this::writeToUpstreamCache);
    }

    @SuppressWarnings("unchecked")
    private Mono<Signal<? extends String>> readFromAvailCache(String key) {
        final Optional<Signal<? extends String>> maybeValue = Optional.ofNullable(this.availCache.get(key)).map(val -> (Signal<String>) val.get());
        return Mono.justOrEmpty(maybeValue);
    }

    private Mono<Void> writeToAvailCache(String key, Signal<? extends String> value) {
        return Mono.fromRunnable(() -> this.availCache.put(key, value));
    }

    @SuppressWarnings("unchecked")
    private Mono<List<Signal<BundledAvailability>>> readFromUpstreamCache(String key) {
        final Optional<List<Signal<BundledAvailability>>> maybeValue = Optional.ofNullable(this.upstreamCache.get(key)).map(val -> (List<Signal<BundledAvailability>>) val.get());
        return Mono.justOrEmpty(maybeValue);
    }

    private Mono<Void> writeToUpstreamCache(String key, List<Signal<BundledAvailability>> value) {
        return Mono.fromRunnable(() -> this.upstreamCache.put(key, value));
    }
}
