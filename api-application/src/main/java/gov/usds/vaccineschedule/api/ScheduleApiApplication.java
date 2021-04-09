package gov.usds.vaccineschedule.api;

import gov.usds.vaccineschedule.api.properties.GeocoderConfigProperties;
import gov.usds.vaccineschedule.api.properties.RollbarConfigProperties;
import gov.usds.vaccineschedule.api.properties.ScheduleSourceConfigProperties;
import gov.usds.vaccineschedule.api.properties.VaccineScheduleProperties;
import gov.usds.vaccineschedule.api.services.ExampleDataService;
import gov.usds.vaccineschedule.api.services.ScheduledTaskService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({
        VaccineScheduleProperties.class,
        RollbarConfigProperties.class,
        GeocoderConfigProperties.class,
        ScheduleSourceConfigProperties.class
})
@EnableCaching
@EnableScheduling
public class ScheduleApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScheduleApiApplication.class, args);
    }

    /**
     * If enabled, schedule the data refresh operations for the upstream sources.
     * Note: If running in a cluster, only a single node should be responsible for managing the chron jobs, otherwise, we'll get duplicate executions
     * The backend queue takes care of parallelizing the work amongst all nodes, but the actual job submission should be sequential
     *
     * @param config    - {@link ScheduleSourceConfigProperties} for configuring
     * @param scheduler - {@link ScheduledTaskService} for scheduling
     * @return - {@link CommandLineRunner} for running
     */
    @Bean
    @ConditionalOnProperty("vs.schedule-source.schedule-enabled")
    public CommandLineRunner scheduleSourceFetch(ScheduleSourceConfigProperties config, ScheduledTaskService scheduler) {
        return args -> scheduler.scheduleFetch(config);
    }

    @Bean
    @ConditionalOnProperty("vs.load-example-data")
    public CommandLineRunner loadExampleData(ExampleDataService service) {
        return args -> service.loadTestData();
    }
}
