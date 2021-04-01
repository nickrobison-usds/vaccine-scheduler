package cov.usds.vaccineschedule.common.models;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.Extension;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Slot;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UrlType;

/**
 * Created by nickrobison on 4/1/21
 */
@ResourceDef(name = "Slot", profile = "http://example.slot")
public class VaccineSlot extends Slot {
    private static final long serialVersionUID = 42L;

    @Extension(url = "http://fhir-registry.smarthealthit.org/StructureDefinition/booking-deep-link", definedLocally = false, isModifier = false)
    @Description(shortDefinition = "a web link into the Provider Booking Portal where the user can begin booking this slot")
    @Child(name = "bookingUrl")
    private UrlType bookingUrl;

    @Extension(url = "http://fhir-registry.smarthealthit.org/StructureDefinition/booking-phone", isModifier = false, definedLocally = false)
    @Description(shortDefinition = "a phone number the user can call to book this slot")
    @Child(name = "bookingPhone")
    private StringType bookingPhone;

    @Extension(url = "http://fhir-registry.smarthealthit.org/StructureDefinition/slot-capacity", isModifier = true, definedLocally = false)
    @Description(shortDefinition = "used to enable aggregated discovery at mass vaccination sites")
    @Child(name = "capacity")
    private IntegerType capacity;


    public UrlType getBookingUrl() {
        return bookingUrl;
    }

    public void setBookingUrl(UrlType bookingUrl) {
        this.bookingUrl = bookingUrl;
    }

    public StringType getBookingPhone() {
        return bookingPhone;
    }

    public void setBookingPhone(StringType bookingPhone) {
        this.bookingPhone = bookingPhone;
    }

    public IntegerType getCapacity() {
        return capacity;
    }

    public void setCapacity(IntegerType capacity) {
        this.capacity = capacity;
    }
}
