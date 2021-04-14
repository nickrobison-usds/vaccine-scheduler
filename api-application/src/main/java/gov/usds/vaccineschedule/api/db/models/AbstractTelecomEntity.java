package gov.usds.vaccineschedule.api.db.models;

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
        this.value = value;
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

    @Override
    protected String getEntityProfile() {
        return "";
    }
}
