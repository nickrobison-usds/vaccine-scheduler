package gov.usds.vaccineschedule.api.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.Count;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Offset;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import gov.usds.vaccineschedule.api.helpers.BaseURLProvider;
import gov.usds.vaccineschedule.api.pagination.AbstractPaginatingProvider;
import gov.usds.vaccineschedule.api.services.ScheduleService;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.Schedule;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by nickrobison on 3/26/21
 */
@Component
public class ScheduleProvider extends AbstractPaginatingProvider<Schedule> {

    private final ScheduleService service;

    public ScheduleProvider(FhirContext ctx, ScheduleService service, BaseURLProvider provider) {
        super(ctx, provider);
        this.service = service;
    }

    @Search
    public Bundle scheduleSearch(
            @OptionalParam(name = Schedule.SP_ACTOR) ReferenceParam locationRef,
            @OptionalParam(name = Schedule.SP_IDENTIFIER) TokenParam identifierParam,
            RequestDetails requestDetails,
            @Offset Integer pageOffset,
            @Count Integer pageSize) {

        final Pageable pageRequest = super.buildPageRequest(pageOffset, pageSize);

        final InstantType searchTime = InstantType.now();
        final long totalCount;
        List<Schedule> schedules;
        if (locationRef != null) {
            totalCount = this.service.countSchedulesForLocation(locationRef.getIdPart());
            schedules = this.service.getSchedulesForLocation(locationRef.getIdPart(), pageRequest);
        } else if (identifierParam != null) {
            totalCount = this.service.countScheduleWithIdentifier(identifierParam);
            schedules = this.service.getScheduleWithIdentifier(identifierParam, pageRequest);
        } else {
            totalCount = this.service.countAllSchedules();
            schedules = this.service.getAllSchedules(pageRequest);
        }

        return super.createBundle(requestDetails, schedules, searchTime, pageRequest, totalCount);
    }

    @Read
    public Schedule getSchedule(@IdParam IIdType id) {
        return this.service.getSchedule(id).orElseThrow(() -> new ResourceNotFoundException(id));
    }

    @Override
    public Class<Schedule> getResourceType() {
        return Schedule.class;
    }
}
