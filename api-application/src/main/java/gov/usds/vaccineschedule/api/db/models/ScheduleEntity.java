package gov.usds.vaccineschedule.api.db.models;

import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Schedule;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static gov.usds.vaccineschedule.common.Constants.ORIGINAL_ID_SYSTEM;
import static gov.usds.vaccineschedule.common.Constants.SCHEDULE_PROFILE;

/**
 * Created by nickrobison on 3/26/21
 */
@Entity
@Table(name = "schedules")
public class ScheduleEntity extends BaseEntity implements Flammable<Schedule> {

    public static Identifier HL7_IDENTIFIER = new Identifier().setSystem("http://terminology.hl7.org/CodeSystem/service-type").setValue("57");
    public static Identifier SMART_IDENTIFIER = new Identifier().setSystem("http://fhir-registry.smarthealthit.org/CodeSystem/service-type").setValue("covid19-immunization");

    @ManyToOne
    @JoinColumn(name = "location_id")
    private LocationEntity location;

    @OneToMany(mappedBy = "entity", orphanRemoval = true, cascade = CascadeType.ALL)
    private Collection<ScheduleIdentifier> identifiers;

    public ScheduleEntity() {
        // Hibernate required
    }

    public LocationEntity getLocation() {
        return location;
    }

    public void setLocation(LocationEntity location) {
        this.location = location;
    }

    public Collection<ScheduleIdentifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Collection<ScheduleIdentifier> identifiers) {
        this.identifiers = identifiers;
    }

    @Override
    public Schedule toFHIR() {
        final Schedule schedule = new Schedule();
        schedule.setMeta(generateMeta());

        schedule.setId(this.getInternalId().toString());
        schedule.addIdentifier(HL7_IDENTIFIER);
        schedule.addIdentifier(SMART_IDENTIFIER);
        this.identifiers.stream().map(ScheduleIdentifier::toFHIR).forEach(schedule::addIdentifier);

        schedule.addActor(new Reference("Location/" + this.location.getInternalId().toString()));

        return schedule;
    }

    public void merge(ScheduleEntity other) {
        this.identifiers.forEach(i -> i.setEntity(null));
        this.identifiers.clear();
        this.identifiers.addAll(other.identifiers);
        this.identifiers.forEach(i -> i.setEntity(this));
    }

    public static ScheduleEntity fromFHIR(LocationEntity location, Schedule resource) {
        final ScheduleEntity entity = new ScheduleEntity();

        entity.setLocation(location);

        final List<ScheduleIdentifier> identifiers = resource.getIdentifier().stream().map(ScheduleIdentifier::fromFHIR).peek(i -> i.setEntity(entity)).collect(Collectors.toList());
        // If we have an existing ID, stash it for later search
        final ScheduleIdentifier origId = new ScheduleIdentifier(ORIGINAL_ID_SYSTEM, resource.getId());
        origId.setEntity(entity);
        identifiers.add(origId);

        entity.setIdentifiers(identifiers);
        return entity;
    }

    @Override
    protected String getEntityProfile() {
        return SCHEDULE_PROFILE;
    }
}
