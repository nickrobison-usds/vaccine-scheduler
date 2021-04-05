package gov.usds.vaccineschedule.api.services;

import ca.uhn.fhir.context.FhirContext;
import gov.usds.vaccineschedule.common.helpers.NDJSONToFHIR;
import gov.usds.vaccineschedule.common.models.VaccineSlot;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by nickrobison on 3/30/21
 */
@Service
@Transactional
public class ExampleDataService {

    private static final Logger logger = LoggerFactory.getLogger(ExampleDataService.class);

    private final NDJSONToFHIR converter;
    private final LocationService locService;
    private final ScheduleService scheduleService;
    private final SlotService slotService;

    public ExampleDataService(FhirContext ctx, LocationService locService, ScheduleService scheduleService, SlotService slotService) {
        this.converter = new NDJSONToFHIR(ctx.newJsonParser());
        this.locService = locService;
        this.scheduleService = scheduleService;
        this.slotService = slotService;
    }


    public void loadTestData() {
        logger.info("Beginning example data loading");
        logger.debug("Loading test location data");
        loadLocations();
        logger.debug("Loading test schedule data");
        loadSchedules();
        logger.debug("Loading test slot data");
        loadSlots();
        logger.info("Finished loading example data");
    }

    private void loadLocations() {
        loadResources(Location.class, "example-locations.ndjson", this.locService::addLocations);
    }

    private void loadSchedules() {
        loadResources(Schedule.class, "example-schedules.ndjson", this.scheduleService::addSchedules);
    }

    private void loadSlots() {
        loadResources(VaccineSlot.class, "example-slots.ndjson", this.slotService::addSlots);
    }

    private <R extends IBaseResource> void loadResources(Class<R> clazz, String fileName, Consumer<Collection<R>> consumer) {
        try (InputStream is = ExampleDataService.class.getClassLoader().getResourceAsStream(fileName)) {
            final List<R> resource = this.converter
                    .inputStreamToTypedResource(clazz, is);
            consumer.accept(resource);


        } catch (IOException e) {
            throw new RuntimeException("Cannot open file");
        }
    }
}
