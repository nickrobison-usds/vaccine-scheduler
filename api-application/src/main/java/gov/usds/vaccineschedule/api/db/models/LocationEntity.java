package gov.usds.vaccineschedule.api.db.models;

import org.apache.commons.codec.digest.DigestUtils;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Meta;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static gov.usds.vaccineschedule.common.Constants.ORIGINAL_ID_SYSTEM;

/**
 * Created by nickrobison on 3/26/21
 */
@Entity
@Table(name = "locations")
public class LocationEntity extends BaseEntity implements Flammable<Location>, Identifiable {

    @Column(nullable = false)
    private String name;
    @Embedded
    private AddressElement address;
    private String locationHash;

    @OneToMany(mappedBy = "entity", fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<LocationIdentifier> identifiers;

    @OneToMany(mappedBy = "entity", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<LocationTelecom> telecoms;

    private Point coordinates;

    @Column(name = "h3_index")
    private long h3Index;

    public LocationEntity() {
        // Hibernate required
    }

    private LocationEntity(String name, AddressElement address, String locationHash) {
        this.name = name;
        this.address = address;
        this.locationHash = locationHash;
    }

    public String getName() {
        return name;
    }

    public AddressElement getAddress() {
        return address;
    }

    public String getLocationHash() {
        return locationHash;
    }

    public Set<LocationIdentifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Set<LocationIdentifier> identifiers) {
        this.identifiers = identifiers;
    }

    public Set<LocationTelecom> getTelecoms() {
        return telecoms;
    }

    public void setTelecoms(Set<LocationTelecom> telecoms) {
        this.telecoms = telecoms;
    }

    public Point getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Point coordinates) {
        this.coordinates = coordinates;
    }

    public long getH3Index() {
        return h3Index;
    }

    public void setH3Index(long h3Index) {
        this.h3Index = h3Index;
    }

    public void merge(LocationEntity other) {
        this.coordinates = other.coordinates;
        this.address = other.address;
        this.name = other.name;
        this.locationHash = other.locationHash;

        // Merge telecoms and identifiers
        this.telecoms.forEach(t -> t.setEntity(null));
        this.telecoms.clear();
        this.telecoms.addAll(other.telecoms);
        this.telecoms.forEach(t -> t.setEntity(this));

        this.identifiers.forEach(i -> i.setEntity(null));
        this.identifiers.clear();
        this.identifiers.addAll(other.identifiers);
        this.identifiers.forEach(i -> i.setEntity(this));

    }

    @Override
    public Location toFHIR() {
        final Location location = new Location();

        if (this.getUpdatedAt() != null) {
            final Meta meta = new Meta();
            final String fhirDateString = this.getUpdatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            meta.setLastUpdatedElement(new InstantType(fhirDateString));
            location.setMeta(meta);
        }

        location.setId(this.getInternalId().toString());
        location.setName(this.name);
        location.setAddress(this.address.toFHIR());

        // Identifiers
        final List<Identifier> identifiers = this.identifiers.stream().map(LocationIdentifier::toFHIR).collect(Collectors.toList());
        location.setIdentifier(identifiers);

        // Telecom as well
        final List<ContactPoint> telecoms = this.telecoms.stream().map(LocationTelecom::toFHIR).collect(Collectors.toList());
        location.setTelecom(telecoms);

        // Add the coordinates, if they exist
        if (this.coordinates != null) {
            final Location.LocationPositionComponent component = new Location.LocationPositionComponent()
                    .setLongitude(this.coordinates.getX())
                    .setLatitude(this.coordinates.getY());
            location.setPosition(component);
        }
        return location;
    }

    public static LocationEntity fromFHIR(Location resource) {

        final AddressElement addressElement = AddressElement.fromFHIR(resource.getAddress());
        final String hash = DigestUtils.sha1Hex(String.format("%s%s", resource.getName(), addressElement.toString()));

        final LocationEntity entity = new LocationEntity(
                resource.getName(),
                addressElement,
                hash);

        // Identifiers
        final Set<LocationIdentifier> identifiers = resource.getIdentifier().stream()
                .map(LocationIdentifier::fromFHIR)
                .peek(i -> i.setEntity(entity))
                .collect(Collectors.toSet());
        final Set<LocationTelecom> telecoms = resource.getTelecom().stream()
                .map(LocationTelecom::fromFHIR)
                .peek(t -> t.setEntity(entity))
                .collect(Collectors.toSet());

        // We need to move the existing Id to a new identifier field
        final LocationIdentifier originalId = new LocationIdentifier(ORIGINAL_ID_SYSTEM, resource.getId());
        originalId.setEntity(entity);
        identifiers.add(originalId);

        entity.setIdentifiers(identifiers);
        entity.setTelecoms(telecoms);

        // If we have a position component, is that as our point
        final Location.LocationPositionComponent position = resource.getPosition();
        if (!position.isEmpty()) {
            final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
            final Point point = factory.createPoint(new Coordinate(position.getLongitude().doubleValue(), position.getLatitude().doubleValue()));
            entity.setCoordinates(point);
        }
        return entity;
    }
}
