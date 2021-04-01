package gov.usds.vaccineschedule.api.models;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.hl7.fhir.r4.model.InstantType;
import org.springframework.data.domain.PageRequest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by nickrobison on 4/1/21
 */
public class JPABundleProvider implements IBundleProvider {

    private final InstantType searchTime;
    private final int size;
    private final String id;
    private final PageFetcher fetcher;

    public JPABundleProvider(int size, PageFetcher fetcher) {
        this.size = size;
        this.id = UUID.randomUUID().toString();
        this.searchTime = InstantType.now();
        this.fetcher = fetcher;
    }


    @Override
    public IPrimitiveType<Date> getPublished() {
        return this.searchTime;
    }

    @Nonnull
    @Override
    public List<IBaseResource> getResources(int theFromIndex, int theToIndex) {
        // Compute page size
        final int pageSize = theToIndex - theFromIndex;
        final PageRequest request = PageRequest.of(theFromIndex / pageSize, pageSize);
        return this.fetcher.fetch(request);
    }

    @Nullable
    @Override
    public String getUuid() {
        return this.id;
    }

    @Override
    public Integer preferredPageSize() {
        return 10;
    }

    @Nullable
    @Override
    public Integer size() {
        return this.size;
    }
}
