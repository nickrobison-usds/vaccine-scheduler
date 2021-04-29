package gov.usds.vaccineschedule.api.db.models;

import gov.usds.vaccineschedule.api.formatters.PhoneFormatter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.DynamicUpdate;
import org.hl7.fhir.r4.model.ContactPoint;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

/**
 * Created by nickrobison on 3/26/21
 */
@DynamicUpdate
@MappedSuperclass
public class AbstractTelecomEntity<T> extends BaseEntity {

    @Column(nullable = false)
    private ContactPoint.ContactPointSystem system;
    @Column(nullable = false)
    private String value;

    @ManyToOne
    @Cascade(value = {org.hibernate.annotations.CascadeType.ALL})
    @JoinColumn(name = "entity_id")
    private T entity;

    public AbstractTelecomEntity() {
        // Hibernate required
    }

    protected AbstractTelecomEntity(ContactPoint.ContactPointSystem system, String value) {
        this.system = system;
        // If we're provided a phone number like value, format it.
        if (shouldNormalizePhone(system)) {
            this.value = PhoneFormatter.formatPhoneNumber(value);
        } else {
            this.value = value;
        }
    }

    public ContactPoint.ContactPointSystem getSystem() {
        return system;
    }

    public String getValue() {
        return value;
    }

    public T getEntity() {
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }

    public ContactPoint toFHIR() {
        return new ContactPoint().setSystem(this.system).setValue(this.value);
    }

    private static boolean shouldNormalizePhone(ContactPoint.ContactPointSystem system) {
        return system.equals(ContactPoint.ContactPointSystem.PHONE)
                || system.equals(ContactPoint.ContactPointSystem.FAX)
                || system.equals(ContactPoint.ContactPointSystem.PAGER)
                || system.equals(ContactPoint.ContactPointSystem.SMS);
    }
}
