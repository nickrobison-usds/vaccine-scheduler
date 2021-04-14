package gov.usds.vaccineschedule.api.services;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.param.TokenParam;
import gov.usds.vaccineschedule.api.BaseApplicationTest;
import gov.usds.vaccineschedule.common.helpers.NDJSONToFHIR;
import gov.usds.vaccineschedule.common.models.VaccineLocation;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Type;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.InputStream;
import java.sql.Date;
import java.util.List;

import static gov.usds.vaccineschedule.common.Constants.CURRENT_AS_OF;
import static gov.usds.vaccineschedule.common.Constants.ORIGINAL_ID_SYSTEM;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Created by nickrobison on 3/30/21
 */
public class TestLocationService extends BaseApplicationTest {

    @Autowired
    LocationService service;

    @Test
    void testLocationUpdate() {

        final IParser parser = ctx.newJsonParser();
        final NDJSONToFHIR converter = new NDJSONToFHIR(parser);

        // Pull a single location out of the NDJSON file
        final InputStream is = TestLocationService.class.getClassLoader().getResourceAsStream("example-locations.ndjson");
        final VaccineLocation firstLoc = converter.inputStreamToTypedResource(VaccineLocation.class, is).get(0);

        // Pull out the location to cache it
        final TokenParam tokenParam = new TokenParam().setSystem(ORIGINAL_ID_SYSTEM).setValue(firstLoc.getId());
        final Location origLocation = this.service.findLocations(tokenParam, null, null, null, Pageable.unpaged()).get(0);
        final Meta meta = new Meta();
        meta.setLastUpdated(Date.valueOf("2020-01-01"));
        firstLoc.setMeta(meta);
        // Update the name and save it
        firstLoc.setName("I'm an updated name");
        service.addLocation(firstLoc);
        // Now, find it and pull back out

        final Location updatedLocation = service.findLocations(tokenParam, null, null, null, PageRequest.of(0, 10)).get(0);
        assertAll(() -> assertEquals(firstLoc.getName(), updatedLocation.getName(), "Should have updated name"),
                () -> assertFalse(getCurrentTimestamp(origLocation).equalsDeep(getCurrentTimestamp(updatedLocation)), "Update timestamp should be different"));
    }

    @Test
    void testLoadNoChange() {
        final long origCount = this.service.countLocations(null, null, null, null);
        final IParser parser = ctx.newJsonParser();
        final NDJSONToFHIR converter = new NDJSONToFHIR(parser);
        final InputStream is = TestLocationService.class.getClassLoader().getResourceAsStream("example-locations.ndjson");
        final List<VaccineLocation> locations = converter.inputStreamToTypedResource(VaccineLocation.class, is);
        this.service.addLocations(locations);
        assertEquals(origCount, this.service.countLocations(null, null, null, null), "Count should not change");
    }

    private static InstantType getCurrentTimestamp(Location location) {
        final Type value = location.getMeta().getExtensionByUrl(CURRENT_AS_OF).getValue();
        return value.castToInstant(value);
    }

}
