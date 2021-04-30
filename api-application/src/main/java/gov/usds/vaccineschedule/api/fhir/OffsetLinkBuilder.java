package gov.usds.vaccineschedule.api.fhir;

import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.apache.http.client.utils.URIBuilder;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nickrobison on 4/5/21
 */
public class OffsetLinkBuilder implements LinkBuilder {

    private static final Logger logger = LoggerFactory.getLogger(OffsetLinkBuilder.class);

    private final String serverBase;
    private final String resource;
    private final RequestDetails requestDetails;
    private final int pageSize;
    private final int pageOffset;
    private final long total;

    public OffsetLinkBuilder(String serverBase, RequestDetails requestDetails, String resource, int pageSize, int pageOffset, long total) {
        this.serverBase = serverBase;
        this.resource = resource;
        this.requestDetails = requestDetails;
        this.pageSize = pageSize;
        this.pageOffset = pageOffset;
        this.total = total;
    }

    @Override
    public boolean isPagingRequested() {
        return this.pageOffset > 0 || this.total > this.pageSize;
    }

    @Override
    public int getPageSize() {
        return this.pageSize;
    }

    @Override
    public boolean isFirstPage() {
        return this.pageOffset == 0;
    }

    @Override
    public void addLinks(Bundle to) {
        to.addLink(
                new Bundle.BundleLinkComponent()
                        .setRelation(Constants.LINK_FIRST)
                        .setUrl(createPageLink(0)));

        if (this.pageOffset + this.pageSize < this.total) {
            to.addLink(
                    new Bundle.BundleLinkComponent()
                            .setRelation(Constants.LINK_NEXT)
                            .setUrl(createPageLink(this.pageOffset + 1)));
        }

        if (!isFirstPage()) {
            to.addLink(
                    new Bundle.BundleLinkComponent()
                            .setRelation(Constants.LINK_PREVIOUS)
                            .setUrl(createPageLink(Math.max(this.pageOffset - 1, 0))));
        }

        /*
         * This formula rounds numTotalResults down to the nearest multiple of pageSize that's less than
         * and not equal to numTotalResults
         */
        int lastIndex;
        try {
            lastIndex = (int) ((total - 1) / pageSize * pageSize);
        } catch (ArithmeticException e) {
            throw new InvalidRequestException(String.format("Invalid pageSize '%s'", pageSize));
        }
        to.addLink(
                new Bundle.BundleLinkComponent()
                        .setRelation(Constants.LINK_LAST)
                        .setUrl(createPageLink(lastIndex)));
    }

    /**
     * Build the link string
     *
     * @param offset page offset
     * @return the link requested
     */
    private String createPageLink(int offset) {

        // Get a copy of all request parameters.
        Map<String, String[]> params = new HashMap<>(requestDetails.getParameters());

        // Add in paging related changes.
        params.put(Constants.PARAM_OFFSET, new String[]{String.valueOf(offset)});
        params.put(Constants.PARAM_COUNT, new String[]{String.valueOf(getPageSize())});

        try {
            URIBuilder uri = new URIBuilder(this.serverBase + resource);

            // Create query parameters by iterating thru all params entry sets. Handle multi values for
            // the same parameter key.
            for (Map.Entry<String, String[]> paramSet : params.entrySet()) {
                for (String param : paramSet.getValue()) {
                    uri.addParameter(paramSet.getKey(), param);
                }
            }
            return uri.build().toString();
        } catch (URISyntaxException e) {
            throw new InvalidRequestException("Invalid URI:" + e);
        }
    }
}
