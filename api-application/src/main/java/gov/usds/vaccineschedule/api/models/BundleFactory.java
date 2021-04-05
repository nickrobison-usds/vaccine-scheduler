package gov.usds.vaccineschedule.api.models;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.Resource;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by nickrobison on 4/5/21
 */
public class BundleFactory {

    public static Bundle createBundle(List<? extends Resource> resources, LinkBuilder builder, InstantType searchTime, long total) {

        final Bundle bundle = new Bundle();
        final List<Bundle.BundleEntryComponent> entries = resources
                .stream()
                .map(r -> new Bundle.BundleEntryComponent().setResource(r))
                .collect(Collectors.toList());

        bundle.setEntry(entries);
        bundle.setTotal((int) total);

        // Add pages
        builder.addLinks(bundle);

        // Search time
        // Should have last updated as well
        bundle.setTimestampElement(searchTime);
        return bundle;

    }
}
