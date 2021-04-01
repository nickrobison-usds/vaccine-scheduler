package gov.usds.vaccineschedule.api.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jaxrs.server.AbstractJaxRsResourceProvider;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import cov.usds.vaccineschedule.common.models.VaccineSlot;
import gov.usds.vaccineschedule.api.services.SlotService;
import org.hl7.fhir.r4.model.Schedule;
import org.hl7.fhir.r4.model.Slot;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Created by nickrobison on 3/29/21
 */
@Component
public class SlotProvider extends AbstractJaxRsResourceProvider<VaccineSlot> {

    private final SlotService service;

    public SlotProvider(FhirContext ctx, SlotService service) {
        super(ctx);
        this.service = service;
    }

    @Search
    public Collection<VaccineSlot> findSlots(@OptionalParam(name = Slot.SP_SCHEDULE + '.' + Schedule.SP_ACTOR) ReferenceParam locationID,
                                             @OptionalParam(name = Slot.SP_START) DateRangeParam dateRange) {
        if (locationID != null) {
            return this.service.getSlotsForLocation(locationID, dateRange);
        } else {
            return service.getSlots();
        }

    }

    @Override
    public Class<VaccineSlot> getResourceType() {
        return VaccineSlot.class;
    }
}
