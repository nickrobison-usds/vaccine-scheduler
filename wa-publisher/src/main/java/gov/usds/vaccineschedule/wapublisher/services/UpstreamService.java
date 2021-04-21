package gov.usds.vaccineschedule.wapublisher.services;

import gov.usds.vaccineschedule.common.models.VaccineLocation;
import gov.usds.vaccineschedule.common.models.VaccineSlot;
import gov.usds.vaccineschedule.wapublisher.models.BundledAvailability;
import gov.usds.vaccineschedule.wapublisher.models.LocationResponse;
import gov.usds.vaccineschedule.wapublisher.models.WALocation;
import graphql.kickstart.spring.webclient.boot.GraphQLRequest;
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Schedule;
import org.hl7.fhir.r4.model.Slot;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UrlType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static gov.usds.vaccineschedule.common.Constants.HL7_SYSTEM;
import static gov.usds.vaccineschedule.common.Constants.SCHEDULE_PROFILE;
import static gov.usds.vaccineschedule.common.Constants.SMART_SYSTEM;
import static gov.usds.vaccineschedule.common.helpers.DateUtils.offsetDateTimeToDate;

/**
 * Created by nickrobison on 4/16/21
 */
@Service
public class UpstreamService {

    private static final Logger logger = LoggerFactory.getLogger(UpstreamService.class);

    private static final String QUERY = "query {\n" +
            "  searchLocations(searchInput: {\n" +
            "    state: \"WA\",\n" +
            "    paging: {\n" +
            "      pageNum: %d,\n" +
            "      pageSize: %d\n" +
            "    }\n" +
            "  }) {\n" +
            "    locations {\n" +
            "      locationId\n" +
            "      locationName\n" +
            "      locationType\n" +
            "      addressLine1\n" +
            "      addressLine2\n" +
            "      city\n" +
            "      state\n" +
            "      zipcode\n" +
            "      latitude\n" +
            "      longitude\n" +
            "      phone\n" +
            "      email\n" +
            "      schedulingLink\n" +
            "      vaccineTypes\n" +
            "      vaccineAvailability\n" +
            "      updatedAt\n" +
            "      }\n" +
            "    paging {\n" +
            "      pageNum\n" +
            "      pageSize\n" +
            "      total\n" +
            "    }\n" +
            "    }\n" +
            "  }";

    private final GraphQLWebClient client;

    public UpstreamService(GraphQLWebClient client) {
        this.client = client;
    }

    public Flux<BundledAvailability> getUpstreamAvailability() {
        final Instant start = Instant.now();
        logger.info("Performing fetch from upstream");

        return requestPage(1, 50)
                .expand(it -> Mono
                        .justOrEmpty(maybeNextPage(it.getPaging()))
                        .flatMap(nextPage -> requestPage(nextPage, 50)))
                .flatMap(locationResponse -> Flux.fromIterable(locationResponse.getLocations()).map(this::availabilityFromLocation))
                .doOnComplete(() -> {
                    final Instant end = Instant.now();
                    final Duration duration = Duration.between(start, end);
                    logger.info("Fetch completed in {}ms", duration.toMillis());
                });

    }

    private Mono<LocationResponse> requestPage(int pageNum, int pageSize) {
        logger.info("Requesting page: {}", pageNum);
        final GraphQLRequest request = GraphQLRequest.builder().query(String.format(QUERY, pageNum, pageSize)).build();
        return client.post(request)
                .map(response -> response.get("searchLocations", LocationResponse.class));
    }

    private BundledAvailability availabilityFromLocation(WALocation waLoc) {
        final VaccineLocation location = new VaccineLocation();
        // Add Meta
        final Meta meta = new Meta();
        meta.setLastUpdated(offsetDateTimeToDate(waLoc.getUpdatedAt()));
        location.setMeta(meta);
        location.setName(waLoc.getLocationName());
        location.setId(waLoc.getLocationId());

        // Address
        final Address address = new Address();
        addIfNotNull(address::addLine, waLoc::getAddressLine1);
        addIfNotNull(address::addLine, waLoc::getAddressLine2);
        addIfNotNull(address::setCity, waLoc::getCity);
        addIfNotNull(address::setState, waLoc::getState);
        addIfNotNull(address::setPostalCode, waLoc::getZipcode);
        location.setAddress(address);

        // Position
        final Location.LocationPositionComponent position = new Location.LocationPositionComponent()
                .setLongitude(waLoc.getLongitude())
                .setLatitude(waLoc.getLatitude());
        location.setPosition(position);

        // Telecoms
        // From the docs:
        // # This field is nullable, but if it's null, then either or both of `email`
        //  # or `schedulingLink` are guaranteed to have non-null values.
        // So we're guaranteed to have a least one contact point.
        if (waLoc.getEmail() != null) {
            final ContactPoint point = new ContactPoint();
            point.setSystem(ContactPoint.ContactPointSystem.EMAIL);
            point.setValue(waLoc.getEmail());
            location.addTelecom(point);
        }

        if (waLoc.getPhone() != null) {
            final ContactPoint point = new ContactPoint();
            point.setSystem(ContactPoint.ContactPointSystem.PHONE);
            point.setValue(waLoc.getPhone());
            location.addTelecom(point);
        }

        if (waLoc.getSchedulingLink() != null) {
            final ContactPoint point = new ContactPoint();
            point.setSystem(ContactPoint.ContactPointSystem.URL);
            point.setValue(waLoc.getSchedulingLink());
            location.addTelecom(point);
        }

        final List<VaccineSlot> slots = slotsFromLocation(waLoc);
        final List<Schedule> schedules = schedulesFromLocation(waLoc);

        return new BundledAvailability(location, schedules, slots);
    }

    private List<VaccineSlot> slotsFromLocation(WALocation location) {
        logger.debug("Translating location {}", location.getLocationId());

        final VaccineSlot slot = new VaccineSlot();
        // Add Meta
        final Meta meta = new Meta();
        meta.setLastUpdated(offsetDateTimeToDate(location.getUpdatedAt()));
        slot.setMeta(meta);
        slot.setId(location.getLocationId());
        if (location.getSchedulingLink() != null) {
            slot.setBookingUrl(new UrlType(location.getSchedulingLink()));
        }
        if (location.getPhone() != null) {
            slot.setBookingPhone(new StringType(location.getPhone()));
        }
        slot.setStatus(statusFromAvailability(location.getVaccineAvailability()));

        // Start/End
        final OffsetDateTime now = OffsetDateTime.now();
        final LocalDateTime start = now.toLocalDate().atStartOfDay();
        slot.setStart(Date.from(start.toInstant(ZoneOffset.UTC)));
        slot.setEnd(Date.from(start.plusDays(1).toInstant(ZoneOffset.UTC)));

        slot.setSchedule(new Reference(new IdType("Schedule", location.getLocationId())));
        return Collections.singletonList(slot);
    }

    private List<Schedule> schedulesFromLocation(WALocation location) {
        final Schedule schedule = new Schedule();

        // Add Meta
        final Meta meta = new Meta();
        meta.addProfile(SCHEDULE_PROFILE);
        meta.setLastUpdated(offsetDateTimeToDate(location.getUpdatedAt()));
        schedule.setMeta(meta);

        schedule.setId(location.getLocationId());
        schedule.addActor(new Reference(new IdType("Location", location.getLocationId())));
        final CodeableConcept concept = new CodeableConcept();
        concept.addCoding().setSystem(HL7_SYSTEM).setCode("57").setDisplay("Immunization");
        concept.addCoding().setSystem(SMART_SYSTEM).setCode("covid19-immunization").setDisplay("COVID-19 Immunization Appointment");
        schedule.addServiceType(concept);

        return Collections.singletonList(schedule);
    }

    private static <T> void addIfNotNull(Consumer<T> consumer, @Nullable Supplier<T> value) {
        if (value != null) {
            consumer.accept(value.get());
        }
    }

    private static Slot.SlotStatus statusFromAvailability(WALocation.LocationAvailability avail) {
        if (avail == WALocation.LocationAvailability.AVAILABLE) {
            return Slot.SlotStatus.FREE;
        }
        return Slot.SlotStatus.BUSY;
    }

    private static @Nullable
    Integer maybeNextPage(LocationResponse.Pagination pagination) {
        final int pageSize = pagination.getPageSize();
        final int pageNum = pagination.getPageNum();
        final int seenSoFar = (pageSize * pageNum);

        if (seenSoFar < pagination.getTotal()) {
            return pageNum + 1;
        }
        return null;
    }
}
