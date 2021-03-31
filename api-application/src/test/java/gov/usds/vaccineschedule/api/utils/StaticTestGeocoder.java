package gov.usds.vaccineschedule.api.utils;

import gov.usds.vaccineschedule.api.db.models.AddressElement;
import gov.usds.vaccineschedule.api.services.geocoder.GeocoderService;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by nickrobison on 3/31/21
 */
public class StaticTestGeocoder implements GeocoderService {

    private static final ConcurrentMap<String, Point> values = loadValues();

    @Override
    public Mono<Point> geocodeLocation(AddressElement address) {
        final String normalized = address.toNormalizedAddress();
        if (!values.containsKey(normalized)) {
            return Mono.error(new IllegalArgumentException(String.format("Cannot location for address: %s\n", normalized)));
        }
        return Mono.just(values.get(normalized));
    }

    private static ConcurrentMap<String, Point> loadValues() {
        final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        final ConcurrentHashMap<String, Point> map = new ConcurrentHashMap<>();

        map.put("123 Summer St Boston, MA 02114", geometryFactory.createPoint(new Coordinate(-71.055954, 42.35284)));
        map.put("123 West St Worcester, MA 01602", geometryFactory.createPoint(new Coordinate(-71.80824, 42.27019)));
        map.put("123 Ash St Springfield, MA 01101", geometryFactory.createPoint(new Coordinate(-72.59757, 42.105992)));
        map.put("123 Arrow St Cambridge, MA 02139", geometryFactory.createPoint(new Coordinate(-71.1154065, 42.3709927)));
        map.put("123 Peach St Lowell, MA 01851", geometryFactory.createPoint(new Coordinate(-71.33, 42.64)));
        map.put("123 Oak St Brockton, MA 02301", geometryFactory.createPoint(new Coordinate(-71.063059, 42.100039)));
        map.put("123 Cyprus St New Bedford, MA 02740", geometryFactory.createPoint(new Coordinate(-70.93, 41.64)));
        map.put("123 Cherry St Lynn, MA 01901", geometryFactory.createPoint(new Coordinate(-70.929738, 42.463469)));
        map.put("123 Cranberry St Quincy, MA 02269", geometryFactory.createPoint(new Coordinate(-71.648608, 42.688602)));
        map.put("123 Elm St Pittsfield, MA 01201", geometryFactory.createPoint(new Coordinate(-73.24056, 42.44476)));
        return map;
    }
}
