package gov.usds.vaccineschedule.api.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jaxrs.server.AbstractJaxRsResourceProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import gov.usds.vaccineschedule.api.helpers.BaseURLProvider;
import gov.usds.vaccineschedule.api.models.BundleFactory;
import gov.usds.vaccineschedule.api.models.OffsetLinkBuilder;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.InstantType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by nickrobison on 4/5/21
 */
public abstract class AbstractPaginatingProvider<T extends IBaseResource> extends AbstractJaxRsResourceProvider<T> {

    private final BaseURLProvider provider;

    public AbstractPaginatingProvider(FhirContext ctx, BaseURLProvider provider) {
        super(ctx);
        this.provider = provider;
    }

    public Pageable buildPageRequest(@Nullable Integer pageOffset, @Nullable Integer pageSize) {
        // If we have a null offset, then we return the first page
        final int page = pageOffset == null ? 0 : pageOffset;
        // This will need to be pull from the configuration
        final int size = pageSize == null ? 50 : pageSize;
        return PageRequest.of(page, size);
    }

    public Bundle createBundle(RequestDetails details, List<T> resources, InstantType searchTime, Pageable page, long totalCount) {
        final String baseUrl = provider.get();
        final OffsetLinkBuilder builder = new OffsetLinkBuilder(baseUrl, details, super.getClass().getSimpleName(), page.getPageSize(), page.getPageNumber(), totalCount);
        return BundleFactory.createBundle(baseUrl, resources, builder, searchTime, totalCount);
    }
}
