package gov.usds.vaccineschedule.api.services;

import com.uber.h3core.H3Core;
import com.uber.h3core.LengthUnit;
import gov.usds.vaccineschedule.api.models.NearestQuery;
import gov.usds.vaccineschedule.api.properties.VaccineScheduleProperties;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Created by nickrobison on 4/8/21
 */
@Service
public class H3Service {
    private static final Logger logger = LoggerFactory.getLogger(H3Service.class);


    private final boolean useH3;
    private final int resolution;
    private final H3Core core;

    public H3Service(VaccineScheduleProperties properties) {
        this.resolution = properties.getH3Resolution();
        this.useH3 = properties.isUseH3Search();
        try {
            this.core = H3Core.newInstance();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        logger.debug("Setting H3 resolution {}", this.resolution);
        if (this.useH3) {
            logger.info("Using H3 methods for approximate searching");
        } else {
            logger.info("Not using H3 for searching");
        }

    }

    /**
     * Encodes the given point to an H3 index.
     * Uses the resolution provided by the configuration
     *
     * @param point - {@link Point} to encode
     * @return - {@link Long} H3 index of point
     */
    public long encodePoint(Point point) {
        return this.core.geoToH3(point.getY(), point.getX(), this.resolution);
    }

    /**
     * Return a {@link List} of H3 indexes which are approximately within the given distance of the point
     *
     * @param query - {@link NearestQuery} to use as search point
     * @return - {@link List} of {@link Long} H3 indexes within the radius
     */
    public List<Long> findNeighbors(NearestQuery query) {
        final long encodedPoint = encodePoint(query.getPoint());
        // Convert the distance to meters and add 10% to try and be more inclusive
        final double searchMeters = query.getDistanceInMeters() * 1.1;
        final int radius = (int) Math.ceil(searchMeters / (core.edgeLength(this.resolution, LengthUnit.m) * 2));
        return this.core.kRing(encodedPoint, radius);
    }

    /**
     * Whether or not to use the H3 indexes as the search method
     *
     * @return - {@code true} Use H3 for searching. {@code false} use normal spatial methods
     */
    public boolean useH3Search() {
        return this.useH3;
    }
}
