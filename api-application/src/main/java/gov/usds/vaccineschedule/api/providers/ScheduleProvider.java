package gov.usds.vaccineschedule.api.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jaxrs.server.AbstractJaxRsResourceProvider;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import gov.usds.vaccineschedule.api.services.ScheduleService;
import org.hl7.fhir.instance.model.api.IIdType;
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
    public Collection<Schedule> scheduleSearch(@OptionalParam(name = Schedule.SP_ACTOR) ReferenceParam locationRef, @OptionalParam(name = Schedule.SP_IDENTIFIER) TokenParam identifierParam) {
        if (locationRef != null) {
            return this.service.getSchedulesForLocation(locationRef.getIdPart());
        } else if (identifierParam != null) {
            return this.service.getScheduleWithIdentifier(identifierParam);
        } else {
            return this.service.getAllSchedules();

        }
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
