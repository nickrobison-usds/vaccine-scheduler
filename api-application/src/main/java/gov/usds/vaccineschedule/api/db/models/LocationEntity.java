package gov.usds.vaccineschedule.api.db.models;

import org.apache.commons.codec.digest.DigestUtils;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Location;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by nickrobison on 3/26/21
 */
@Entity(name = "locations")
public class LocationEntity extends BaseEntity implements Flammable<Location>, Identifiable {

    @Column(nullable = false)
    private String name;
    @Embedded
    private AddressElement address;
    private String locationHash;

    @OneToMany(mappedBy = "entity", fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
    private Collection<LocationIdentifier> identifiers;

    @OneToMany(mappedBy = "entity", orphanRemoval = true, cascade = CascadeType.ALL)
    private Collection<LocationTelecom> telecoms;

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

    public Collection<LocationIdentifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Collection<LocationIdentifier> identifiers) {
        this.identifiers = identifiers;
    }

    public Collection<LocationTelecom> getTelecoms() {
        return telecoms;
    }

    public void setTelecoms(Collection<LocationTelecom> telecoms) {
        this.telecoms = telecoms;
    }

    @Override
    public Location toFHIR() {
        final Location location = new Location();

        location.setId(this.getInternalId().toString());
        location.setName(this.name);
        location.setAddress(this.address.toFHIR());

        // Identifiers
        final List<Identifier> identifiers = this.identifiers.stream().map(LocationIdentifier::toFHIR).collect(Collectors.toList());
        location.setIdentifier(identifiers);

        // Telecom as well
        final List<ContactPoint> telecoms = this.telecoms.stream().map(LocationTelecom::toFHIR).collect(Collectors.toList());
        location.setTelecom(telecoms);
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
        final List<LocationIdentifier> identifiers = resource.getIdentifier().stream()
                .map(LocationIdentifier::fromFHIR)
                .peek(i -> i.setEntity(entity))
                .collect(Collectors.toList());
        final List<LocationTelecom> telecoms = resource.getTelecom().stream()
                .map(LocationTelecom::fromFHIR)
                .peek(t -> t.setEntity(entity))
                .collect(Collectors.toList());
        entity.setIdentifiers(identifiers);
        entity.setTelecoms(telecoms);
        return entity;
    }
}
