package gov.usds.vaccineschedule.api.services;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.param.TokenParam;
import gov.usds.vaccineschedule.api.BaseApplicationTest;
import gov.usds.vaccineschedule.common.helpers.NDJSONToFHIR;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Schedule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

import java.io.InputStream;
import java.util.List;

import static gov.usds.vaccineschedule.common.Constants.ORIGINAL_ID_SYSTEM;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by nickrobison on 4/7/21
 */
public class TestScheduleService extends BaseApplicationTest {

    @Autowired
    private ScheduleService service;

    @Test
    void testScheduleUpdate() {
        final IParser parser = ctx.newJsonParser();
        final NDJSONToFHIR converter = new NDJSONToFHIR(parser);

        // Pull a single location out of the NDJSON file
        final InputStream is = TestLocationService.class.getClassLoader().getResourceAsStream("example-schedules.ndjson");
        final Schedule firstSchedule = converter.inputStreamToTypedResource(Schedule.class, is).get(0);
        firstSchedule.setIdentifier(List.of(new Identifier().setSystem("http://terminology.hl7.org/CodeSystem/service-type2").setValue("57")));
        this.service.addSchedule(firstSchedule);
        final TokenParam tokenParam = new TokenParam().setSystem(ORIGINAL_ID_SYSTEM).setValue(firstSchedule.getId());
        final Schedule updatedSchedule = service.getScheduleWithIdentifier(tokenParam, Pageable.unpaged()).get(0);
        assertEquals(4, updatedSchedule.getIdentifier().size(), "Should only have a single identifier");
    }

    @Test
    void testLoadNoChange() {
        final IParser parser = ctx.newJsonParser();
        final NDJSONToFHIR converter = new NDJSONToFHIR(parser);

        final long origCount = this.service.countAllSchedules();

        // Pull a single location out of the NDJSON file
        final InputStream is = TestLocationService.class.getClassLoader().getResourceAsStream("example-schedules.ndjson");
        final List<Schedule> schedules = converter.inputStreamToTypedResource(Schedule.class, is);
        this.service.addSchedules(schedules);
        assertEquals(origCount, this.service.countAllSchedules(), "Count should not change");
    }
}
