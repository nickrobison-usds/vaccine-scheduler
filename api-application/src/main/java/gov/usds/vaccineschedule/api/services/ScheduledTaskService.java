package gov.usds.vaccineschedule.api.services;

import gov.usds.vaccineschedule.api.config.ScheduleSourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by nickrobison on 3/25/21
 */
@Service
public class ScheduledTaskService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskService.class);

    private final TaskScheduler scheduler;
    private final SourceFetchService fetcher;

    public ScheduledTaskService(SourceFetchService fetchService, TaskScheduler taskScheduler) {
        this.scheduler = taskScheduler;
        this.fetcher = fetchService;
    }

    public Map<String, ScheduledFuture<?>> scheduleFetch(ScheduleSourceConfig config) {
        Map<String, ScheduledFuture<?>> futures = new HashMap<>();
        // Ideally, we should have refresh schedules per source, but that's not necessary for an MVP.
        TimeZone tz = config.getScheduleTimezone();
        for (String cron : config.getRefreshSchedule()) {
            logger.info("Scheduling data refresh to run on cron schedule '{}' in time zone {}", cron, tz.getID());
            Trigger cronTrigger = new CronTrigger(cron, tz);
            futures.put(cron, scheduler.schedule(fetcher::refreshSources, cronTrigger));
        }
        return futures;
    }
}
