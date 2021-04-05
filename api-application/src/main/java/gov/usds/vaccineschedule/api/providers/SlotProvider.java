package gov.usds.vaccineschedule.api.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jaxrs.server.AbstractJaxRsResourceProvider;
import ca.uhn.fhir.rest.annotation.Count;
import ca.uhn.fhir.rest.annotation.Offset;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import gov.usds.vaccineschedule.api.helpers.BaseURLProvider;
import gov.usds.vaccineschedule.api.models.BundleFactory;
import gov.usds.vaccineschedule.api.models.OffsetLinkBuilder;
import gov.usds.vaccineschedule.api.services.SlotService;
import gov.usds.vaccineschedule.common.models.VaccineSlot;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.Schedule;
import org.hl7.fhir.r4.model.Slot;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by nickrobison on 3/29/21
 */
@Component
public class SlotProvider extends AbstractJaxRsResourceProvider<VaccineSlot> {

    private final SlotService service;
    private final BaseURLProvider baseUrl;

    public SlotProvider(FhirContext ctx, SlotService service, BaseURLProvider baseURLProvider) {
        super(ctx);
        this.service = service;
        this.baseUrl = baseURLProvider;
    }

    @Search
    public Bundle findSlots(@OptionalParam(name = Slot.SP_IDENTIFIER) TokenParam slotIdentifier,
                            @OptionalParam(name = Slot.SP_SCHEDULE + '.' + Schedule.SP_ACTOR) ReferenceParam locationID,
                            @OptionalParam(name = Slot.SP_START) DateRangeParam dateRange,
                            RequestDetails requestDetails,
                            @Offset Integer pageOffset,
                            @Count Integer pageSize) {

        // If we have a null offset, then we return the first page
        pageOffset = pageOffset == null ? 0 : pageOffset;
        // This will need to be pull from the configuration
        pageSize = pageSize == null ? 50 : pageSize;

        final InstantType searchTime = InstantType.now();
        final long totalCount;
        List<VaccineSlot> slots;
        if (locationID != null) {
            totalCount = this.service.countSlotsForLocation(locationID, dateRange);
            slots = this.service.getSlotsForLocation(locationID, dateRange, pageOffset, pageSize);
        } else if (slotIdentifier != null) {
            totalCount = service.countSlotsWithId(slotIdentifier);
            slots = this.service.findSlotsWithId(slotIdentifier, pageOffset, pageSize);
        } else {
            totalCount = service.countSlots();
            slots = service.getSlots(pageOffset, pageSize);
        }
        final OffsetLinkBuilder builder = new OffsetLinkBuilder(baseUrl.get(), requestDetails, "Slot", pageSize, pageOffset, totalCount);
        return BundleFactory.createBundle(slots, builder, searchTime, totalCount);
    }

    @Override
    public Class<VaccineSlot> getResourceType() {
        return VaccineSlot.class;
    }
}
