package gov.usds.vaccineschedule.api.models;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Resource;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by nickrobison on 4/5/21
 */
public class BundleFactory {

    public static Bundle createBundle(String baseUrl, List<? extends IBaseResource> resources, LinkBuilder builder, InstantType searchTime, long total) {

        final Consumer<? super IBaseResource> resetter = resetURL(baseUrl);

        final Bundle bundle = new Bundle();
        final List<Bundle.BundleEntryComponent> entries = resources
                .stream()
                .peek(resetter)
                .map(r -> {
                    final Bundle.BundleEntryComponent c = new Bundle.BundleEntryComponent().setResource((Resource) r);
                    c.setId(((Resource) r).getId());
                    return c;
                })
                .collect(Collectors.toList());

        bundle.setEntry(entries);
        bundle.setTotal((int) total);

        // Add pages
        builder.addLinks(bundle);

        // Search time

        bundle.setTimestampElement(searchTime);
        // Also set the last updated time to the newest element in the bundle
        // This is what BFD does, seems like a good idea.
        final Meta meta = new Meta();
        resources
                .stream()
                .map(r -> r.getMeta().getLastUpdated())
                .filter(Objects::nonNull)
                .max(Date::compareTo)
                .map(meta::setLastUpdated);
        bundle.setMeta(meta);
        return bundle;
    }

    private static Consumer<? super IBaseResource> resetURL(String baseUrl) {
        return (r) -> {
            IIdType elem = r.getIdElement();
            if (!elem.hasBaseUrl()) {
                elem = new IdType(r.getIdElement().getValue()).withServerBase(baseUrl, r.getClass().getSimpleName());
                r.setId(elem);
            }
        };
    }
}
