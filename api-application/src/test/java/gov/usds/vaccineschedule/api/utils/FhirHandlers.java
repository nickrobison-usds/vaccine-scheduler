package gov.usds.vaccineschedule.api.utils;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import gov.usds.vaccineschedule.common.models.VaccineSlot;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by nickrobison on 4/5/21
 */
public class FhirHandlers {

    @SuppressWarnings("unchecked")
    public static <T extends Resource> List<T> unwrapBundle(IGenericClient client, Bundle bundle, Date searchTime) {
        int idx = 0;
        assertBundleUpdatedBefore(bundle, searchTime);
        List<T> resources = new ArrayList<>();
        // Loop through all the pages
        bundle.getEntry().forEach(e -> resources.add((T) e.getResource()));

        while (bundle.getLink(Bundle.LINK_NEXT) != null) {
            idx++;
            bundle = client.loadPage().next(bundle).preferResponseType(VaccineSlot.class).execute();
            assertBundleUpdatedBefore(bundle, searchTime);
            bundle.getEntry().forEach(e -> resources.add((T) e.getResource()));
        }

        return resources;
    }

    private static void assertBundleUpdatedBefore(Bundle bundle, Date searchTime) {
        if (!bundle.getEntry().isEmpty()) {
            Assertions.assertTrue(bundle.getMeta().getLastUpdatedElement().before(searchTime), "Updated value should be before search time");
        }

    }
}
