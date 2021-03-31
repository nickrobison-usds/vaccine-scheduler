package gov.usds.vaccineschedule.api.services.geocoder;

import gov.usds.vaccineschedule.api.db.models.AddressElement;
import org.locationtech.jts.geom.Point;
import reactor.core.publisher.Mono;

public interface GeocoderService {

    Mono<Point> geocodeLocation(AddressElement address);
}
