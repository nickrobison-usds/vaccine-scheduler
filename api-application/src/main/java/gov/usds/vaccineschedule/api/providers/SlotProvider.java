package gov.usds.vaccineschedule.api.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.Count;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.Offset;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.validation.FhirValidator;
import gov.usds.vaccineschedule.api.helpers.BaseURLProvider;
import gov.usds.vaccineschedule.api.pagination.AbstractPaginatingAndValidatingProvider;
import gov.usds.vaccineschedule.api.services.SlotService;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.Schedule;
import org.hl7.fhir.r4.model.Slot;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

import static gov.usds.vaccineschedule.common.Constants.SLOT_PROFILE;

/**
 * Created by nickrobison on 3/29/21
 */
@Component
public class SlotProvider extends AbstractPaginatingAndValidatingProvider<Slot> {

    private final SlotService service;

    public SlotProvider(FhirContext ctx, SlotService service, FhirValidator validator, BaseURLProvider baseURLProvider) {
        super(ctx, validator, baseURLProvider);
        this.service = service;
    }

    @Search
    public Bundle findSlots(@OptionalParam(name = Slot.SP_IDENTIFIER) TokenParam slotIdentifier,
                            @OptionalParam(name = Slot.SP_SCHEDULE + '.' + Schedule.SP_ACTOR) ReferenceOrListParam locationIDs,
                            @OptionalParam(name = Slot.SP_START) DateRangeParam dateRange,
                            @IncludeParam(allow = {"Slot:schedule"}) String include,
                            RequestDetails requestDetails,
                            @Offset Integer pageOffset,
                            @Count Integer pageSize) {

        final Pageable pageRequest = super.buildPageRequest(pageOffset, pageSize);
        final InstantType searchTime = InstantType.now();
        final long totalCount;
        List<Slot> slots;
        if (locationIDs != null) {
            final List<ReferenceParam> idParams = locationIDs.getValuesAsQueryTokens();
            totalCount = this.service.countSlotsForLocations(idParams, dateRange);
            slots = this.service.getSlotsForLocations(idParams, dateRange, pageRequest, "Slot:schedule".equals(include));
        } else if (slotIdentifier != null) {
            totalCount = service.countSlotsWithId(slotIdentifier);
            slots = this.service.findSlotsWithId(slotIdentifier, pageRequest, "Slot:schedule".equals(include));
        } else {
            totalCount = service.countSlots();
            slots = service.getSlots(pageRequest, "Slot:schedule".equals(include));
        }

        return super.createBundle(requestDetails, slots, searchTime, pageRequest, totalCount);
    }

    @Override
    public Class<Slot> getResourceType() {
        return Slot.class;
    }

    @Override
    public String getResourceProfile() {
        return SLOT_PROFILE;
    }
}
