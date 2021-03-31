package gov.usds.vaccineschedule.api.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jaxrs.server.AbstractJaxRsResourceProvider;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.ReferenceParam;
import gov.usds.vaccineschedule.api.services.ScheduleService;
import org.hl7.fhir.r4.model.Schedule;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Created by nickrobison on 3/26/21
 */
@Component
public class ScheduleProvider extends AbstractJaxRsResourceProvider<Schedule> {

    private final ScheduleService service;

    public ScheduleProvider(FhirContext ctx, ScheduleService service) {
        super(ctx);
        this.service = service;
    }

    @Search
    public Collection<Schedule> scheduleSearch(@OptionalParam(name = Schedule.SP_ACTOR) ReferenceParam locationRef) {
        if (locationRef == null) {
            return this.service.getSchedule();
        } else {
            return this.service.getSchedulesForLocation(locationRef.getIdPart());
        }
    }

    @Override
    public Class<Schedule> getResourceType() {
        return Schedule.class;
    }
}
