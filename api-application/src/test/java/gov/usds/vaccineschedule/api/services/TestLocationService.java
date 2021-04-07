package gov.usds.vaccineschedule.api.services;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.param.TokenParam;
import gov.usds.vaccineschedule.api.BaseApplicationTest;
import gov.usds.vaccineschedule.common.helpers.NDJSONToFHIR;
import org.hl7.fhir.r4.model.Location;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.io.InputStream;
import java.util.List;

import static gov.usds.vaccineschedule.common.Constants.ORIGINAL_ID_SYSTEM;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
        final Location firstLoc = converter.inputStreamToTypedResource(Location.class, is).get(0);
        // Update the name and save it
        firstLoc.setName("I'm an updated name");
        service.addLocation(firstLoc);
        // Now, find it and pull back out
        final TokenParam tokenParam = new TokenParam().setSystem(ORIGINAL_ID_SYSTEM).setValue(firstLoc.getId());
        final Location updatedLocation = service.findLocations(tokenParam, null, null, null, PageRequest.of(0, 10)).get(0);
        assertEquals(firstLoc.getName(), updatedLocation.getName(), "Should have updated name");
    }

    @Test
    void testLoadNoChange() {
        final long origCount = this.service.countLocations(null, null, null, null);
        final IParser parser = ctx.newJsonParser();
        final NDJSONToFHIR converter = new NDJSONToFHIR(parser);
        final InputStream is = TestLocationService.class.getClassLoader().getResourceAsStream("example-locations.ndjson");
        final List<Location> locations = converter.inputStreamToTypedResource(Location.class, is);
        this.service.addLocations(locations);
        assertEquals(origCount, this.service.countLocations(null, null, null, null), "Count should not change");
    }

}
