package gov.usds.vaccineschedule.api.helpers;

import org.slf4j.MDC;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.util.context.ContextView;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by nickrobison on 4/29/21
 */
public class LoggingReactorFactory {

    private final Function<ContextView, Map<String, String>> contextMapper;

    public LoggingReactorFactory(Function<ContextView, Map<String, String>> contextMapper) {
        this.contextMapper = contextMapper;
    }

    private static ContextCloser withLoggingContext(Map<String, String> vals) {
        final Map<String, String> mdxMap = MDC.getCopyOfContextMap();
        if (mdxMap != null) {
            mdxMap.putAll(vals);
        } else {
            MDC.setContextMap(vals);
        }
        return MDC::clear;
    }

    public <T> Consumer<Signal<T>> logOnNext(Consumer<T> action) {
        return signal -> {
            if (signal.isOnNext()) {
                try (ContextCloser ignored = withLoggingContext(this.contextMapper.apply(signal.getContextView()))) {
                    action.accept(signal.get());
                }
            }
        };
    }

    public <T> Consumer<Signal<T>> logWithSubscriber(@Nullable Consumer<? super T> consumer, @Nullable Consumer<? super Throwable> errorConsumer, @Nullable Runnable completeConsumer) {
        return signal -> {
            try (ContextCloser ignored = withLoggingContext(this.contextMapper.apply(signal.getContextView()))) {
                if (signal.isOnNext() && consumer != null) {
                    consumer.accept(signal.get());
                } else if (signal.isOnError() && errorConsumer != null) {
                    errorConsumer.accept(signal.getThrowable());
                } else if (signal.isOnComplete() && completeConsumer != null) {
                    completeConsumer.run();
                }
            }
        };
    }

    public static <T, R> Mono<R> mapLogging(Mono<T> upstream, Function<T, R> mapper) {
        return upstream
                .handle((element, sink) -> {
                    try (MDC.MDCCloseable closeable = MDC.putCloseable("test-id", "1")) {
                        sink.next(mapper.apply(element));
                    }
                });
    }

    public static <T, R> Mono<R> flatMapLogging(Mono<T> upstream, Function<T, Mono<R>> mapper) {
        return upstream
                .materialize()
                .flatMap(signal -> {
                    if (signal.isOnNext()) {
                        try (MDC.MDCCloseable closeable = MDC.putCloseable("test-id", "1")) {
                            return mapper.apply(signal.get())
                                    .map(Signal::next);
                        }
                    } else {
                        return Mono.fromSupplier(signal);
                    }
                })
                .dematerialize();
    }
}
