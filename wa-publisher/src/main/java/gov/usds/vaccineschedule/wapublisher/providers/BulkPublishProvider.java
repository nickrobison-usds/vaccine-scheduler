package gov.usds.vaccineschedule.wapublisher.providers;

import gov.usds.vaccineschedule.common.models.PublishResponse;
import gov.usds.vaccineschedule.wapublisher.properties.PublisherConfigProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Created by nickrobison on 4/16/21
 */
@RestController
public class BulkPublishProvider {

    private final PublisherConfigProperties properties;

    public BulkPublishProvider(PublisherConfigProperties properties) {
        this.properties = properties;
    }


    @GetMapping("$bulk-publish")
    public PublishResponse publish() {
        // Create some dummy data for each resource type
        final List<PublishResponse.OutputEntry> output = List.of(
                new PublishResponse.OutputEntry("Schedule", buildURL("/test-schedule.ndjson")),
                new PublishResponse.OutputEntry("Location", buildURL("/test-location.ndjson")),
                new PublishResponse.OutputEntry("Slot", buildURL("/test-slot.ndjson"))
        );

        final PublishResponse publishResponse = new PublishResponse();
        publishResponse.setTransactionTime(OffsetDateTime.now());
        publishResponse.setRequest("/$bulk-publish");
        publishResponse.setOutput(output);

        return publishResponse;
    }

    private String buildURL(String resource) {
        return String.format("%s/data%s", this.properties.getBaseURL(), resource);
    }
}
