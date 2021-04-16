package gov.usds.vaccineschedule.wapublisher.models;

import gov.usds.vaccineschedule.common.models.VaccineLocation;
import gov.usds.vaccineschedule.common.models.VaccineSlot;
import org.hl7.fhir.r4.model.Schedule;

import java.util.List;

/**
 * Created by nickrobison on 4/16/21
 */
public class BundledAvailability {

    private final VaccineLocation location;
    private final List<Schedule> schedules;
    private final List<VaccineSlot> slots;

    public BundledAvailability(VaccineLocation location, List<Schedule> schedules, List<VaccineSlot> slots) {
        this.location = location;
        this.schedules = schedules;
        this.slots = slots;
    }

    public VaccineLocation getLocation() {
        return location;
    }

    public List<Schedule> getSchedules() {
        return schedules;
    }

    public List<VaccineSlot> getSlots() {
        return slots;
    }
}
