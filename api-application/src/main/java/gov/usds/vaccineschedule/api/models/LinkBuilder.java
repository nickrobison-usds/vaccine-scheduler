package gov.usds.vaccineschedule.api.models;

import org.hl7.fhir.r4.model.Bundle;

/**
 * Very helpfully pulled from CMS BFD server: https://github.com/CMSgov/beneficiary-fhir-data/blob/8dedd432046acce2e2a5f254331110afd3c2989a/apps/bfd-server/bfd-server-war/src/main/java/gov/cms/bfd/server/war/commons/LinkBuilder.java#L4
 */
public interface LinkBuilder {

    /**
     * Is paging requested?
     *
     * @return true iff paging is requested
     */
    boolean isPagingRequested();

    /**
     * Return the size of the page (ie. _count value). Integer.MAX_VALUE if isPagingRequested is
     * false.
     */
    int getPageSize();

    /**
     * Return is this a first page request. Always true if isPagingReuested() is false.
     */
    boolean isFirstPage();

    /**
     * Add the links from the builder to the bundle. No links are are added if paging is not
     * requested.
     *
     * @param to bundle
     */
    void addLinks(Bundle to);
}
