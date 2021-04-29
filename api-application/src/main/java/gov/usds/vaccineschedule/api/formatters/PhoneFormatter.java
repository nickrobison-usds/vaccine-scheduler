package gov.usds.vaccineschedule.api.formatters;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.springframework.stereotype.Component;

/**
 * Created by nickrobison on 4/29/21
 */
@Component
public class PhoneFormatter {

    private PhoneFormatter() {
        // Not used
    }

    /**
     * Format provided phone number according to {@link com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat#NATIONAL} standards
     *
     * @param phoneNumber - {@link String} supplied phone number to parse
     * @return - {@link String} formatted phone number
     * @throws IllegalArgumentException if the provided phone number is invalid
     */
    public static String formatPhoneNumber(String phoneNumber) {
        try {
            final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            final Phonenumber.PhoneNumber parsed = phoneUtil.parse(phoneNumber, "US");
            return phoneUtil.format(parsed, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
        } catch (NumberParseException e) {
            throw new IllegalArgumentException(String.format("`%s` is not a valid phone number", phoneNumber), e);
        }
    }
}
