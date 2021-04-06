package gov.usds.vaccineschedule.api.utils;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import gov.usds.vaccineschedule.common.models.VaccineSlot;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nickrobison on 4/5/21
 */
public class FhirHandlers {

    @SuppressWarnings("unchecked")
    public static <T extends Resource> List<T> unwrapBundle(IGenericClient client, Bundle bundle) {
        List<T> resources = new ArrayList<>();
        // Loop through all the pages
        bundle.getEntry().forEach(e -> resources.add((T) e.getResource()));

        while (bundle.getLink(Bundle.LINK_NEXT) != null) {
            bundle = client.loadPage().next(bundle).preferResponseType(VaccineSlot.class).execute();
            bundle.getEntry().forEach(e -> resources.add((T) e.getResource()));
        }

        return resources;
    }
}
