package gov.usds.vaccineschedule.api.db.models;

import cov.usds.vaccineschedule.common.models.VaccineSlot;
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
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static gov.usds.vaccineschedule.api.db.models.Constants.ORIGINAL_ID_SYSTEM;

/**
 * Created by nickrobison on 3/29/21
 */
@Entity
@Table(name = "slots")
public class SlotEntity extends BaseEntity implements Flammable<VaccineSlot> {

    private static final ZoneId UTC = ZoneId.of("GMT");
    private static final DateTimeFormatter FHIR_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSVV");

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

    @Override
    public VaccineSlot toFHIR() {
        final VaccineSlot slot = new VaccineSlot();

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

        return slot;
    }

    public static SlotEntity fromFHIR(ScheduleEntity schedule, VaccineSlot resource) {
        final SlotEntity entity = new SlotEntity();

        entity.setSchedule(schedule);

        final List<SlotIdentifier> identifiers = resource.getIdentifier().stream().map(SlotIdentifier::fromFHIR).peek(i -> i.setEntity(entity)).collect(Collectors.toList());

        // If we have an existing ID, stash it for later search
        final SlotIdentifier origId = new SlotIdentifier(ORIGINAL_ID_SYSTEM, resource.getId());
        origId.setEntity(entity);
        identifiers.add(origId);
        entity.setStartTime(OffsetDateTime.parse(resource.getStartElement().getValueAsString(), FHIR_FORMATTER));
        entity.setEndTime(OffsetDateTime.parse(resource.getEndElement().getValueAsString(), FHIR_FORMATTER));
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
