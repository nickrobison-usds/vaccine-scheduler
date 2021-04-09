package gov.usds.vaccineschedule.api.utils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by nickrobison on 4/9/21
 */
public class HelperAssertions {

    public static void assertBDApproxEqual(BigDecimal exp, BigDecimal obs, double epsilon) {
        final BigDecimal eps = BigDecimal.valueOf(epsilon);
        assertTrue(exp.subtract(obs).abs().compareTo(eps) < 0, String.format("`%s` is not within `%.2f` of `%s`", obs.toString(), epsilon, exp.toString()));
    }
}
