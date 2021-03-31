package gov.usds.vaccineschedule.api.services.geocoder;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by nickrobison on 3/31/21
 */
@Entity
public class GeocoderResponseEntity {

    @Id
    public long id;
}
