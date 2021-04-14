package gov.usds.vaccineschedule.api.db.models;

import org.hibernate.annotations.DynamicUpdate;
import org.hl7.fhir.r4.model.Identifier;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import java.util.Objects;

/**
 * Created by nickrobison on 3/26/21
 */
@DynamicUpdate
@MappedSuperclass
public class AbstractIdentifierEntity<T> extends BaseEntity {

    @Column(nullable = false)
    private String system;
    @Column(nullable = false)
    private String value;

    @ManyToOne
    @JoinColumn(name = "entity_id")
    private T entity;

    public AbstractIdentifierEntity() {
        // Hibernate required
    }

    protected AbstractIdentifierEntity(String system, String value) {
        this.system = system;
        this.value = value;
    }

    public String getSystem() {
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

    public Identifier toFHIR() {
        return new Identifier()
                .setSystem(this.system)
                .setValue(this.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractIdentifierEntity<?> that = (AbstractIdentifierEntity<?>) o;
        return system.equals(that.system) && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(system, value);
    }

    @Override
    protected String getEntityProfile() {
        return "";
    }
}
