package gov.usds.vaccineschedule.publisher.providers;

import gov.usds.vaccineschedule.publisher.models.PublishResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Created by nickrobison on 3/25/21
 */
@RestController
public class PublishProvider {


    @RequestMapping("$bulk-publish")
    public PublishResponse publish() {
        // Create some dummy data for each resource type
        final List<PublishResponse.OutputEntry> output = List.of(
                new PublishResponse.OutputEntry("Schedule", "/test-schedule.ndjson"),
                new PublishResponse.OutputEntry("Location", "/test-location.ndjson"),
                new PublishResponse.OutputEntry("Slot", "/test-slot.ndjson")
        );

        final PublishResponse publishResponse = new PublishResponse();
        publishResponse.setTransactionTime(OffsetDateTime.now());
        publishResponse.setRequest("/$bulk-publish");
        publishResponse.setOutput(output);

        return publishResponse;
    }
}
