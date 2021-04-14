package gov.usds.vaccineschedule.api.db.models;

import gov.usds.vaccineschedule.common.Constants;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.Meta;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.time.OffsetDateTime;

import static gov.usds.vaccineschedule.common.Constants.CURRENT_AS_OF;
import static gov.usds.vaccineschedule.common.Constants.FHIR_FORMATTER;

/**
 * Created by nickrobison on 4/14/21
 */
@MappedSuperclass
public abstract class UpstreamUpdateableEntity extends BaseEntity {

    @Column
    protected OffsetDateTime upstreamUpdatedAt;

    public OffsetDateTime getUpstreamUpdatedAt() {
        return upstreamUpdatedAt;
    }

    protected Meta generateMeta(String profile) {
        final Meta meta = new Meta();
        meta.addProfile(profile);

        if (this.getUpstreamUpdatedAt() != null) {
            final String fhirDateString = this.getUpstreamUpdatedAt().format(FHIR_FORMATTER);
            meta.setLastUpdatedElement(new InstantType(fhirDateString));
        }

        if (this.getUpdatedAt() != null) {
            final String fhirDateString = this.getUpdatedAt().format(FHIR_FORMATTER);
            final InstantType currentTimestamp = new InstantType(fhirDateString);
            meta.addExtension(new Extension(CURRENT_AS_OF, currentTimestamp));
        }

        return meta;
    }

    protected void updateFromMeta(Meta meta) {
        // If we have an updated timestamp, set that
        final InstantType lastUpdatedElement = meta.getLastUpdatedElement();
        if (!lastUpdatedElement.isEmpty()) {
            this.upstreamUpdatedAt = OffsetDateTime.parse(lastUpdatedElement.getValueAsString(), Constants.INSTANT_FORMATTER);
        }
    }
}
