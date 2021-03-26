package gov.usds.vaccineschedule.api.db.models;

import org.apache.commons.codec.digest.DigestUtils;
import org.hl7.fhir.r4.model.Location;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;

/**
 * Created by nickrobison on 3/26/21
 */
@Entity(name = "locations")
public class LocationEntity extends BaseEntity implements Flammable<Location> {

    @Column(nullable = false)
    private String name;
    @Embedded
    private AddressElement address;
    private String locationHash;

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

    @Override
    public Location toFHIR() {
        final Location location = new Location();

        location.setId(this.getInternalId().toString());
        location.setName(this.name);
        location.setAddress(this.address.toFHIR());

        // Telecom as well....
        return location;
    }

    public static LocationEntity fromFHIR(Location resource) {

        final AddressElement addressElement = AddressElement.fromFHIR(resource.getAddress());
        final String hash = DigestUtils.sha1Hex(String.format("%s%s", resource.getName(), addressElement.toString()));
        return new LocationEntity(
                resource.getName(),
                addressElement,
                hash);
    }
}
