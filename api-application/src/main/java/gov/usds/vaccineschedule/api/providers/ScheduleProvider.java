package gov.usds.vaccineschedule.api.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jaxrs.server.AbstractJaxRsResourceProvider;
import ca.uhn.fhir.rest.annotation.Search;
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
    public Collection<Schedule> scheduleSearch() {
        return this.service.getSchedule();
    }

    @Override
    public Class<Schedule> getResourceType() {
        return Schedule.class;
    }
}
