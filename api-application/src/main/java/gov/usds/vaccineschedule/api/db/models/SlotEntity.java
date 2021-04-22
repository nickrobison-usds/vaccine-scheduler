package gov.usds.vaccineschedule.api.db.models;

import gov.usds.vaccineschedule.common.Constants;
import gov.usds.vaccineschedule.common.models.VaccineSlot;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Slot;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UrlType;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static gov.usds.vaccineschedule.common.Constants.FOR_LOCATION;
import static gov.usds.vaccineschedule.common.Constants.ORIGINAL_ID_SYSTEM;
import static gov.usds.vaccineschedule.common.Constants.SLOT_PROFILE;

/**
 * Created by nickrobison on 3/29/21
 */
@Entity
@Table(name = "slots")
public class SlotEntity extends UpstreamUpdateableEntity implements Flammable<VaccineSlot> {

    private static final ZoneId UTC = ZoneId.of("GMT");

    @ManyToOne
    @JoinColumn(name = "schedule_id")
    private ScheduleEntity schedule;

    @OneToMany(mappedBy = "entity", orphanRemoval = true, cascade = CascadeType.ALL)
    private Collection<SlotIdentifier> identifiers;

    @Column(nullable = false)
    private OffsetDateTime startTime;
    @Column(nullable = false)
    private OffsetDateTime endTime;

    @Column(nullable = false)
    private Slot.SlotStatus status;

    // Optional extensions that MAY be included
    private String bookingUrl;
    private String bookingPhone;

    @Min(value = 0L, message = "Capacity must be positive")
    private Integer capacity = 1;

    public SlotEntity() {
        // Hibernate required
    }

    public ScheduleEntity getSchedule() {
        return schedule;
    }

    public void setSchedule(ScheduleEntity schedule) {
        this.schedule = schedule;
    }

    public Collection<SlotIdentifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Collection<SlotIdentifier> identifiers) {
        this.identifiers = identifiers;
    }

    public OffsetDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(OffsetDateTime start) {
        this.startTime = start;
    }

    public OffsetDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(OffsetDateTime end) {
        this.endTime = end;
    }

    public Slot.SlotStatus getStatus() {
        return status;
    }

    public void setStatus(Slot.SlotStatus status) {
        this.status = status;
    }

    public String getBookingUrl() {
        return bookingUrl;
    }

    public void setBookingUrl(String bookingUrl) {
        this.bookingUrl = bookingUrl;
    }

    public String getBookingPhone() {
        return bookingPhone;
    }

    public void setBookingPhone(String bookingPhone) {
        this.bookingPhone = bookingPhone;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }


    public VaccineSlot toFHIR(boolean includeSchedule) {
        final VaccineSlot slot = new VaccineSlot();
        slot.setMeta(generateMeta(SLOT_PROFILE));

        slot.setId(this.getInternalId().toString());
        this.identifiers.stream().map(SlotIdentifier::toFHIR).forEach(slot::addIdentifier);
        slot.setSchedule(new Reference("Schedule/" + this.schedule.getInternalId().toString()));
        slot.setStart(Date.from(this.startTime.toLocalDate().atStartOfDay(UTC).toInstant()));
        slot.setStartElement(new InstantType(this.startTime.toInstant().toString()));
        slot.setEndElement(new InstantType(this.endTime.toInstant().toString()));
        slot.setStatus(this.getStatus());

        // Vaccine Slot extensions
        if (bookingUrl != null) {
            slot.setBookingUrl(new UrlType(bookingUrl));
        }
        if (bookingPhone != null) {
            slot.setBookingPhone(new StringType(bookingPhone));
        }
        slot.setCapacity(new IntegerType(capacity));
        // This is a nasty hack to get things moving along
        if (includeSchedule) {
//            slot.getSchedule().setResource(schedule.toFHIR());
        }
        final IdType locationId = new IdType("Location", this.schedule.getLocation().getInternalId().toString());
        slot.addExtension().setUrl(FOR_LOCATION).setValue(locationId);

        return slot;
    }

    @Override
    public VaccineSlot toFHIR() {
        return toFHIR(false);
    }

    public void merge(SlotEntity other) {
        this.bookingPhone = other.bookingPhone;
        this.bookingUrl = other.bookingUrl;
        this.capacity = other.capacity;
        this.status = other.status;
        this.startTime = other.startTime;
        this.endTime = other.endTime;
        this.upstreamUpdatedAt = other.upstreamUpdatedAt;

        this.identifiers.forEach(i -> i.setEntity(null));
        this.identifiers.clear();
        this.identifiers.addAll(other.identifiers);
        this.identifiers.forEach(i -> i.setEntity(this));
    }

    public static SlotEntity fromFHIR(ScheduleEntity schedule, VaccineSlot resource) {
        final SlotEntity entity = new SlotEntity();
        entity.updateFromMeta(resource.getMeta());

        entity.setSchedule(schedule);

        final List<SlotIdentifier> identifiers = resource.getIdentifier().stream().map(SlotIdentifier::fromFHIR).peek(i -> i.setEntity(entity)).collect(Collectors.toList());

        // If we have an existing ID, stash it for later search
        final SlotIdentifier origId = new SlotIdentifier(ORIGINAL_ID_SYSTEM, resource.getId());
        origId.setEntity(entity);
        identifiers.add(origId);
        entity.setStartTime(OffsetDateTime.parse(resource.getStartElement().getValueAsString(), Constants.INSTANT_FORMATTER));
        entity.setEndTime(OffsetDateTime.parse(resource.getEndElement().getValueAsString(), Constants.INSTANT_FORMATTER));
        entity.setStatus(resource.getStatus());

        // Vaccine slot extensions
        if (!resource.getBookingUrl().isEmpty()) {
            entity.setBookingUrl(resource.getBookingUrl().asStringValue());
        }
        if (!resource.getBookingPhone().isEmpty()) {
            entity.setBookingPhone(resource.getBookingPhone().getValueAsString());
        }
        if (!resource.getCapacity().isEmpty()) {
            entity.setCapacity(resource.getCapacity().getValue());
        }

        entity.setIdentifiers(identifiers);
        return entity;
    }
}
