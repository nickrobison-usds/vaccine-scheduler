package gov.usds.vaccineschedule.api.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jaxrs.server.AbstractJaxRsResourceProvider;
import ca.uhn.fhir.rest.annotation.Search;
import gov.usds.vaccineschedule.api.services.SlotService;
import org.hl7.fhir.r4.model.Slot;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Created by nickrobison on 3/29/21
 */
@Component
public class SlotProvider extends AbstractJaxRsResourceProvider<Slot> {

    private final SlotService service;

    public SlotProvider(FhirContext ctx, SlotService service) {
        super(ctx);
        this.service = service;
    }

    @Search
    public Collection<Slot> findSlots() {
        return service.getSlots();
    }

    @Override
    public Class<Slot> getResourceType() {
        return Slot.class;
    }
}
