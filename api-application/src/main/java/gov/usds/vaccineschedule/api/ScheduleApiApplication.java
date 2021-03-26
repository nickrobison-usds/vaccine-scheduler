package gov.usds.vaccineschedule.api;

import gov.usds.vaccineschedule.api.config.ScheduleSourceConfig;
import gov.usds.vaccineschedule.api.services.ScheduledTaskService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableConfigurationProperties({
        ScheduleSourceConfig.class
})
@EnableScheduling
public class ScheduleApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScheduleApiApplication.class, args);
    }

    @Bean
    @ConditionalOnProperty("vs.schedule-source.schedule-enabled")
    public CommandLineRunner scheduleSourceFetch(ScheduleSourceConfig config, ScheduledTaskService scheduler) {
        return args -> scheduler.scheduleFetch(config);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
