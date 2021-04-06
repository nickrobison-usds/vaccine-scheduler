package gov.usds.vaccineschedule.api.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.Count;
import ca.uhn.fhir.rest.annotation.Offset;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import gov.usds.vaccineschedule.api.helpers.BaseURLProvider;
import gov.usds.vaccineschedule.api.pagination.AbstractPaginatingProvider;
import gov.usds.vaccineschedule.api.services.SlotService;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.Schedule;
import org.hl7.fhir.r4.model.Slot;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by nickrobison on 3/29/21
 */
@Component
public class SlotProvider extends AbstractPaginatingProvider<Slot> {

    private final SlotService service;

    public SlotProvider(FhirContext ctx, SlotService service, BaseURLProvider baseURLProvider) {
        super(ctx, baseURLProvider);
        this.service = service;
    }

    @Search
    public Bundle findSlots(@OptionalParam(name = Slot.SP_IDENTIFIER) TokenParam slotIdentifier,
                            @OptionalParam(name = Slot.SP_SCHEDULE + '.' + Schedule.SP_ACTOR) ReferenceParam locationID,
                            @OptionalParam(name = Slot.SP_START) DateRangeParam dateRange,
                            RequestDetails requestDetails,
                            @Offset Integer pageOffset,
                            @Count Integer pageSize) {

        final Pageable pageRequest = super.buildPageRequest(pageOffset, pageSize);
        final InstantType searchTime = InstantType.now();
        final long totalCount;
        List<Slot> slots;
        if (locationID != null) {
            totalCount = this.service.countSlotsForLocation(locationID, dateRange);
            slots = this.service.getSlotsForLocation(locationID, dateRange, pageRequest);
        } else if (slotIdentifier != null) {
            totalCount = service.countSlotsWithId(slotIdentifier);
            slots = this.service.findSlotsWithId(slotIdentifier, pageRequest);
        } else {
            totalCount = service.countSlots();
            slots = service.getSlots(pageRequest);
        }

        return super.createBundle(requestDetails, slots, searchTime, pageRequest, totalCount);
    }

    @Override
    public Class<Slot> getResourceType() {
        return Slot.class;
    }
}
