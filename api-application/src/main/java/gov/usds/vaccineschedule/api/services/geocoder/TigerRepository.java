package gov.usds.vaccineschedule.api.services.geocoder;

import gov.usds.vaccineschedule.api.db.models.AddressElement;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * Created by nickrobison on 3/31/21
 */
@Repository
public class TigerRepository {

    private final EntityManager em;

    public TigerRepository(@Qualifier("tigerEntityManager") EntityManager em) {
        this.em = em;
    }

    public String geocoder(AddressElement address) {
        final Query query = em.createNativeQuery("SELECT ROW_NUMBER() OVER() as id, g.rating, ST_X(g.geomout) as lon, ST_Y(g.geomout) as lat FROM geocode(normalize_address(?1), 100) as g", GeocoderResponseEntity.class);
        query.setParameter(1, "4849 Connecticut Ave NW Washington, DC");
        final List resultList = query.getResultList();
        query.getParameters();
        return null;
    }
}
