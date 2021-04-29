package gov.usds.vaccineschedule.api.services;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.param.TokenParam;
import gov.usds.vaccineschedule.api.BaseApplicationTest;
import gov.usds.vaccineschedule.common.helpers.NDJSONToFHIR;
import gov.usds.vaccineschedule.common.models.VaccineSlot;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.StringType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.io.InputStream;
import java.sql.Date;
import java.util.List;

import static gov.usds.vaccineschedule.common.Constants.FOR_LOCATION;
import static gov.usds.vaccineschedule.common.Constants.ORIGINAL_ID_SYSTEM;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by nickrobison on 4/7/21
 */
public class TestSlotService extends BaseApplicationTest {

    @Autowired
    SlotService service;

    @Test
    void testSlotUpdate() {
        final IParser parser = ctx.newJsonParser();
        final NDJSONToFHIR converter = new NDJSONToFHIR(parser);

        // Pull a single location out of the NDJSON file
        final InputStream is = TestLocationService.class.getClassLoader().getResourceAsStream("example-slots.ndjson");
        final VaccineSlot firstSlot = converter.inputStreamToTypedResource(VaccineSlot.class, is).get(0);
        // Set the upstream updated time
        final Date updatedDate = Date.valueOf("2020-01-01");
        final Meta m1 = new Meta().setLastUpdated(updatedDate);
        firstSlot.setMeta(m1);
        firstSlot.setCapacity(new IntegerType(5));
        firstSlot.setBookingPhone(new StringType("5550000000"));
        service.addSlot(firstSlot);
        final TokenParam tokenParam = new TokenParam().setSystem(ORIGINAL_ID_SYSTEM).setValue(firstSlot.getId());
        final VaccineSlot updatedSlot = (VaccineSlot) service.findSlotsWithId(tokenParam, PageRequest.of(0, 10), true).get(0);
        assertAll(() -> assertTrue(firstSlot.getCapacity().equalsDeep(updatedSlot.getCapacity()), "Should have updated capacity"),
                () -> assertTrue(firstSlot.getBookingUrl().equalsDeep(updatedSlot.getBookingUrl()), "Should have original url"),
                () -> assertEquals(updatedDate, updatedSlot.getMeta().getLastUpdated(), "Should have updated upstream"),
                () -> assertEquals("(555) 000-0000", updatedSlot.getBookingPhone().getValue(), "Should have updated phone number"),
                () -> assertNotNull(updatedSlot.getExtensionByUrl(FOR_LOCATION), "Should have location extension"));
    }

    @Test
    void testLoadNoChange() {
        final long origCount = this.service.countSlots();

        final IParser parser = ctx.newJsonParser();
        final NDJSONToFHIR converter = new NDJSONToFHIR(parser);
        final InputStream is = TestLocationService.class.getClassLoader().getResourceAsStream("example-slots.ndjson");
        final List<VaccineSlot> slots = converter.inputStreamToTypedResource(VaccineSlot.class, is);
        this.service.addSlots(slots);
        assertEquals(origCount, this.service.countSlots(), "Count should not change");
    }
}
