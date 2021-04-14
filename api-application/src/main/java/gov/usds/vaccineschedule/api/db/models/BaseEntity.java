package gov.usds.vaccineschedule.api.db.models;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.Meta;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Created by nickrobison on 3/26/21
 */
@MappedSuperclass
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Column(updatable = false, nullable = false)
    @Id
    @GeneratedValue(generator = "UUID4")
    private UUID internalId;

    @Column(updatable = false)
    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Column
    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    public UUID getInternalId() {
        return internalId;
    }

    public void setInternalId(UUID internalId) {
        this.internalId = internalId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    protected Meta generateMeta() {
        final Meta meta = new Meta();
        meta.addProfile(getEntityProfile());

        if (this.getUpdatedAt() != null) {
            final String fhirDateString = this.getUpdatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            meta.setLastUpdatedElement(new InstantType(fhirDateString));
        }
        return meta;
    }

    protected void updateFromMeta(Meta meta) {

    }

    protected abstract String getEntityProfile();
}
