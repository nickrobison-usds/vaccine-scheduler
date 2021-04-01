package gov.usds.vaccineschedule.api.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jaxrs.server.AbstractJaxRsResourceProvider;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import gov.usds.vaccineschedule.api.models.JPABundleProvider;
import gov.usds.vaccineschedule.api.models.PageFetcher;
import gov.usds.vaccineschedule.api.services.SlotService;
import gov.usds.vaccineschedule.common.models.VaccineSlot;
import org.hl7.fhir.r4.model.Schedule;
import org.hl7.fhir.r4.model.Slot;
import org.springframework.stereotype.Component;

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
    public IBundleProvider findSlots(@OptionalParam(name = Slot.SP_IDENTIFIER) TokenParam slotIdentifier,
                                     @OptionalParam(name = Slot.SP_SCHEDULE + '.' + Schedule.SP_ACTOR) ReferenceParam locationID,
                                     @OptionalParam(name = Slot.SP_START) DateRangeParam dateRange) {

        final PageFetcher fetcher;
        if (locationID != null) {
            fetcher = (page) -> this.service.getSlotsForLocation(locationID, dateRange, page);
        } else if (slotIdentifier != null) {
            fetcher = (page) -> service.findSlots(slotIdentifier, page);
        } else {
            fetcher = service::getSlots;
        }

        return new JPABundleProvider(100, fetcher);
    }

    @Override
    public Class<VaccineSlot> getResourceType() {
        return VaccineSlot.class;
    }
}
