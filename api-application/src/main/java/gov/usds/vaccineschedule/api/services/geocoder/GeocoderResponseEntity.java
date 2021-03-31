package gov.usds.vaccineschedule.api.services.geocoder;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by nickrobison on 3/31/21
 */
@Entity
public class GeocoderResponseEntity {

    @Id
    private long id;
    private Integer rating;
    private double longitude;
    private double latitude;


    GeocoderResponseEntity() {
        // Hibernate required
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
