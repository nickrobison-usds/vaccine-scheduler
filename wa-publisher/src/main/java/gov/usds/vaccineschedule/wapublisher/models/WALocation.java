package gov.usds.vaccineschedule.wapublisher.models;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;

/**
 * Created by nickrobison on 4/16/21
 */
public class WALocation {

    private String locationID;
    private String locationName;
    @Nullable
    private String locationType;
    @Nullable
    private String addressLine1;
    @Nullable
    private String addressLine2;
    @Nullable
    private String city;
    @Nullable
    private String state;
    private String zipcode;
    private double latitude;
    private double longitude;
    @Nullable
    private String phone;
    @Nullable
    private String email;
    @Nullable
    private String schedulingLink;
    private OffsetDateTime updatedAt;
    private LocationAvailability vaccineAvailability;

    public WALocation() {
        // Jackson required
    }

    public String getLocationID() {
        return locationID;
    }

    public String getLocationName() {
        return locationName;
    }

    @Nullable
    public String getLocationType() {
        return locationType;
    }

    @Nullable
    public String getAddressLine1() {
        return addressLine1;
    }

    @Nullable
    public String getAddressLine2() {
        return addressLine2;
    }

    @Nullable
    public String getCity() {
        return city;
    }

    @Nullable
    public String getState() {
        return state;
    }

    public String getZipcode() {
        return zipcode;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Nullable
    public String getPhone() {
        return phone;
    }

    @Nullable
    public String getEmail() {
        return email;
    }

    @Nullable
    public String getSchedulingLink() {
        return schedulingLink;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocationAvailability getVaccineAvailability() {
        return vaccineAvailability;
    }

    public enum LocationAvailability {
        AVAILABLE,
        UNAVAILABLE,
        UNKNOWN
    }
}
