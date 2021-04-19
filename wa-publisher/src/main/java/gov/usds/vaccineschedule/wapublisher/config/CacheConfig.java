package gov.usds.vaccineschedule.wapublisher.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Created by nickrobison on 4/19/21
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Configuration
public class CacheConfig {

    @Bean
    public Caffeine caffeineConfig() {
        return Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES);
    }

    @Bean
    public CacheManager managerConfig(Caffeine caffeine) {
        final CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(caffeine);
        return cacheManager;
    }
}
