package gov.usds.vaccineschedule.api.services;

import gov.usds.vaccineschedule.api.db.models.AddressElement;
import org.locationtech.jts.geom.Point;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * Created by nickrobison on 3/30/21
 */
public class DBGeocoderService implements GeocoderService {

    private final EntityManager em;


    public DBGeocoderService(EntityManager em) {
        this.em = em;
    }


    @Override
    @Transactional(readOnly = true)
    public Mono<Point> geocodeLocation(AddressElement address) {
        return Mono.fromCallable(() -> {
            final Query query = em.createNativeQuery("SELECT g.rating, ST_X(g.geomout) as lon, ST_Y(g.geomout) as lat FROM tiger.geocode(CAST(tiger.normalize_address(?1) as tiger.norm_addy), 1) as g");
            query.setParameter(1, address.getStreet().get(0));
            final int i = query.executeUpdate();
            query.getParameters();
            return null;
        });

    }
}
