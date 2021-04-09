package gov.usds.vaccineschedule.api.services.geocoder;

import gov.usds.vaccineschedule.api.db.models.AddressElement;
import gov.usds.vaccineschedule.api.properties.GeocoderConfigProperties;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Created by nickrobison on 3/30/21
 */
public class MapBoxGeocoderService implements GeocoderService {
    private static final Logger logger = LoggerFactory.getLogger(MapBoxGeocoderService.class);

    private final GeocoderConfigProperties config;

    public MapBoxGeocoderService(GeocoderConfigProperties config) {
        logger.info("Enabling Mapbox Geocoding service");
        this.config = config;
    }

    @Override
    public Mono<Point> geocodeLocation(AddressElement address) {
        return WebClient.create("https://api.mapbox.com/geocoding/v5/mapbox.places/")
                .get()
                .uri(builder -> {
                    builder.queryParam("access_token", this.config.getMapboxToken());
                    builder.path(String.format("%s.json", address.toNormalizedAddress()));
                    return builder.build();
                })
                .retrieve()
                .bodyToMono(FeatureCollection.class)
                .map(response -> {

                    if (response.getFeatures().isEmpty()) {
                        throw new IllegalArgumentException("No coordinates returned from Geocoder");
                    }

                    final Feature feature = response.getFeatures().get(0);
                    final LngLatAlt coords = ((org.geojson.Point) feature.getGeometry()).getCoordinates();
                    final Coordinate coordinate = new Coordinate(coords.getLongitude(), coords.getLatitude(), coords.getAltitude());

                    final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
                    final Point point = factory.createPoint(coordinate);
                    logger.info("Geocoded {} to {}", address.toNormalizedAddress(), point);
                    return point;
                });
    }
}
