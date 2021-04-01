package gov.usds.vaccineschedule.api.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static tech.units.indriya.unit.Units.METRE;

/**
 * Created by nickrobison on 4/1/21
 */
public class NearestQueryTest {

    @Test
    public void testValidQueries() {
        // Just the point
        final NearestQuery single = NearestQuery.fromToken("42.4887|-71.2837");
        assertAll(() -> assertEquals(-71.2837, single.getPoint().getX(), "Should have point"),
                () -> assertEquals(10000, convertToMeters(single), "Should converted distance"));

        // With custom distance
        final NearestQuery distance = NearestQuery.fromToken("42.4887|-71.2837|50");
        assertAll(() -> assertEquals(-71.2837, distance.getPoint().getX(), "Should have point"),
                () -> assertEquals(50000, convertToMeters(distance), "Should converted distance"));

        // With custom units
        final NearestQuery km = NearestQuery.fromToken("42.4887|-71.2837|50|km");
        assertAll(() -> assertEquals(-71.2837, km.getPoint().getX(), "Should have point"),
                () -> assertEquals(50000, convertToMeters(km), "Should converted distance"));

        final NearestQuery miles = NearestQuery.fromToken("42.4887|-71.2837|50|mi");
        assertAll(() -> assertEquals(-71.2837, miles.getPoint().getX(), "Should have point"),
                () -> assertEquals(80467.2, convertToMeters(miles), "Should converted distance"));
    }


    @Test
    public void testInvalidQueries() {
        assertThrows(IllegalArgumentException.class, () -> NearestQuery.fromToken(""), "Should throw with no data");
        assertThrows(IllegalArgumentException.class, () -> NearestQuery.fromToken("452|"), "Should throw with no data");

        // Test incorrect unit
        final IllegalArgumentException exn = assertThrows(IllegalArgumentException.class, () -> NearestQuery.fromToken("42.4887|-71.2837|50|inch"), "Should throw with inches");
        assertEquals("Unsupported unit: inch not recognized (in inch at index 0)", exn.getMessage(), "Should have correct message");

        // Test with bad coordinates
        final IllegalArgumentException badCoords = assertThrows(IllegalArgumentException.class, () -> NearestQuery.fromToken("42.4887|bad"), "Should throw with non-double coordinate");
        assertEquals("Cannot parse: `For input string: \"bad\"` as coordinate.", badCoords.getMessage(), "Should have correct message");

        // Test with bad distance
        final IllegalArgumentException badDistance = assertThrows(IllegalArgumentException.class, () -> NearestQuery.fromToken("42.4887|-71.2837|bad|mi"), "Should throw with bad distance");
        assertEquals("Cannot parse: `For input string: \"bad\"` as coordinate.", badDistance.getMessage(), "Should have correct error message");

    }

    public static double convertToMeters(NearestQuery query) {
        return query.getDistance().to(METRE).getValue().doubleValue();
    }
}
