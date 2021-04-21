package gov.usds.vaccineschedule.api.services;

import ca.uhn.fhir.context.FhirContext;
import gov.usds.vaccineschedule.api.BaseApplicationTest;
import gov.usds.vaccineschedule.api.properties.ScheduleSourceConfigProperties;
import gov.usds.vaccineschedule.common.models.PublishResponse;
import gov.usds.vaccineschedule.common.models.VaccineLocation;
import gov.usds.vaccineschedule.common.models.VaccineSlot;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Schedule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static gov.usds.vaccineschedule.common.Constants.FHIR_NDJSON;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by nickrobison on 3/26/21
 */
public class FetchServiceTest extends BaseApplicationTest {

    private static MockWebServer mockWebServer;


    @Autowired
    private FhirContext ctx;
    private LocationService lService;
    private ScheduleService scheduleService;
    private SlotService slotService;
    private SourceFetchService service;

    private String baseUrl;

    @BeforeAll
    static void startMockServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void stopMockServer() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void setupFetchService() {
        baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        lService = Mockito.mock(LocationService.class);
        scheduleService = Mockito.mock(ScheduleService.class);
        slotService = Mockito.mock(SlotService.class);
        final ScheduleSourceConfigProperties config = new ScheduleSourceConfigProperties(false, List.of(baseUrl), Collections.emptyList(), TimeZone.getDefault(), 2);
        service = new SourceFetchService(ctx, config, lService, scheduleService, slotService);
    }

    @AfterEach
    void restTheMocks() {
        Mockito.reset(lService, scheduleService, slotService);
    }

    @Test
    void testMockService() throws IOException, InterruptedException {
        // Setup requests
        final List<PublishResponse.OutputEntry> output = List.of(
                new PublishResponse.OutputEntry("Schedule", buildURL("/test-schedule.ndjson")),
                new PublishResponse.OutputEntry("Location", buildURL("/test-location.ndjson")),
                new PublishResponse.OutputEntry("Slot", buildURL("/test-slot.ndjson")));

        final PublishResponse publishResponse = new PublishResponse();
        publishResponse.setTransactionTime(OffsetDateTime.now());
        publishResponse.setRequest("/$bulk-publish");
        publishResponse.setOutput(output);

        // Locations
        final InputStream lIS = FetchServiceTest.class.getClassLoader().getResourceAsStream("example-locations.ndjson");
        assertNotNull(lIS);
        final String locations = IOUtils.toString(lIS, StandardCharsets.UTF_8);
        final MockResponse locResponse = new MockResponse()
                .setBody(locations)
                .addHeader("Content-Type", FHIR_NDJSON);
        mockWebServer.enqueue(locResponse);


        // Schedules
        final InputStream sIS = FetchServiceTest.class.getClassLoader().getResourceAsStream("example-schedules.ndjson");
        assertNotNull(sIS);
        final String schedules = IOUtils.toString(sIS, StandardCharsets.UTF_8);
        final MockResponse schedResponse = new MockResponse()
                .setBody(schedules)
                .addHeader("Content-Type", FHIR_NDJSON);
        mockWebServer.enqueue(schedResponse);

        // Slots
        final InputStream slotIS = FetchServiceTest.class.getClassLoader().getResourceAsStream("example-slots.ndjson");
        assertNotNull(slotIS);
        final String slots = IOUtils.toString(slotIS, StandardCharsets.UTF_8);
        final MockResponse slotResponse = new MockResponse()
                .setBody(slots)
                .addHeader("Content-Type", FHIR_NDJSON);
        mockWebServer.enqueue(slotResponse);

        final WebClient client = WebClient.create(baseUrl);

        final Mono<PublishResponse> just = Mono.just(publishResponse);
        final List<IBaseResource> resources = service.buildResourceFetcher(client, just).collect(Collectors.toList())
                .block();

        assertNotNull(resources);
        assertEquals(90, resources.size(), "Should have resources");

        // Verify the order is correct
        final Optional<IBaseResource> nonLocations = resources.subList(0, 10).stream().filter(r -> !r.getClass().equals(VaccineLocation.class)).findAny();
        assertTrue(nonLocations.isEmpty(), "Should only have locations");
        final Optional<IBaseResource> nonSchedules = resources.subList(10, 20).stream().filter(r -> !r.getClass().equals(Schedule.class)).findAny();
        assertTrue(nonSchedules.isEmpty(), "Should only have schedules");
        final Optional<IBaseResource> nonSlots = resources.subList(20, 90).stream().filter(r -> !r.getClass().equals(VaccineSlot.class)).findAny();
        assertTrue(nonSlots.isEmpty(), "Should only have slots");


        // First, locations
        final RecordedRequest firstReq = mockWebServer.takeRequest();
        assertAll(() -> assertEquals("GET", firstReq.getMethod()),
                () -> assertEquals("/data/test-location.ndjson", firstReq.getPath()));

        // Schedules next
        final RecordedRequest second = mockWebServer.takeRequest();
        assertAll(() -> assertEquals("GET", second.getMethod()),
                () -> assertEquals("/data/test-schedule.ndjson", second.getPath()));

        // Finally, slots
        final RecordedRequest third = mockWebServer.takeRequest();
        assertAll(() -> assertEquals("GET", third.getMethod()),
                () -> assertEquals("/data/test-slot.ndjson", third.getPath()));
    }

    private String buildURL(String resource) {
        return String.format("%s/data%s", baseUrl, resource);
    }
}
