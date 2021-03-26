package gov.usds.vaccineschedule.api;

import gov.usds.vaccineschedule.api.config.ScheduleSourceConfig;
import gov.usds.vaccineschedule.api.services.ScheduledTaskService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({
        ScheduleSourceConfig.class
})
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
     * @param config    - {@link ScheduleSourceConfig} for configuring
     * @param scheduler - {@link ScheduledTaskService} for scheduling
     * @return - {@link CommandLineRunner} for running
     */
    @Bean
    @ConditionalOnProperty("vs.schedule-source.schedule-enabled")
    public CommandLineRunner scheduleSourceFetch(ScheduleSourceConfig config, ScheduledTaskService scheduler) {
        return args -> scheduler.scheduleFetch(config);
    }
}
