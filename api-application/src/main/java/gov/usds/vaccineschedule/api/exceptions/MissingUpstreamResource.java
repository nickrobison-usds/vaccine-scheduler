package gov.usds.vaccineschedule.api.exceptions;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Identifier;

/**
 * Created by nickrobison on 4/21/21
 */
public class MissingUpstreamResource extends RuntimeException {

    private final Class<? extends IBaseResource> resourceType;
    private final Identifier resourceIdentifier;

    public MissingUpstreamResource(Class<? extends IBaseResource> resourceType, Identifier resourceIdentifier) {
        this.resourceType = resourceType;
        this.resourceIdentifier = resourceIdentifier;
    }

    public Class<? extends IBaseResource> getResourceType() {
        return resourceType;
    }

    public Identifier getResourceIdentifier() {
        return resourceIdentifier;
    }
}
