package gov.usds.vaccineschedule.api.services.geocoder;

import gov.usds.vaccineschedule.api.db.models.AddressElement;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * Created by nickrobison on 4/9/21
 */
public class NoOpGeocoder implements GeocoderService {
    private static final Logger logger = LoggerFactory.getLogger(NoOpGeocoder.class);

    private final Point noopPoint;

    public NoOpGeocoder() {
        logger.warn("No Geocoder configured, spatial search functions disabled");
        final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
        this.noopPoint = factory.createPoint(new Coordinate(0, 0));
    }


    @Override
    public Mono<Point> geocodeLocation(AddressElement address) {
        return Mono.just(this.noopPoint);
    }
}
