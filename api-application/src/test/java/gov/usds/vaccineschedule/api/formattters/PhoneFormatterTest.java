package gov.usds.vaccineschedule.api.formattters;

import gov.usds.vaccineschedule.api.formatters.PhoneFormatter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created by nickrobison on 4/29/21
 */
public class PhoneFormatterTest {

    private static final String EXPECTED_NUMBER = "(555) 555-5555";

    @Test
    public void testCorrectFormat() {
        assertAll(() -> assertEquals(EXPECTED_NUMBER, PhoneFormatter.formatPhoneNumber("5555555555")),
                () -> assertEquals(EXPECTED_NUMBER, PhoneFormatter.formatPhoneNumber("555-555-5555")),
                () -> assertEquals(EXPECTED_NUMBER, PhoneFormatter.formatPhoneNumber(EXPECTED_NUMBER)),
                () -> assertEquals(EXPECTED_NUMBER, PhoneFormatter.formatPhoneNumber("+1 555 555-5555")));
    }

    @Test
    public void testInvalidPhoneNumber() {
        final IllegalArgumentException exn = assertThrows(IllegalArgumentException.class, () -> PhoneFormatter.formatPhoneNumber("Not a number"));
        assertEquals("`Not a number` is not a valid phone number", exn.getMessage(), "Should have correct error message");
    }
}
