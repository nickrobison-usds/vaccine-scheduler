package gov.usds.vaccineschedule.api.models;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import systems.uom.unicode.CLDR;
import tech.units.indriya.format.SimpleUnitFormat;
import tech.units.indriya.quantity.Quantities;

import javax.measure.MetricPrefix;
import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.format.MeasurementParseException;
import javax.measure.quantity.Length;
import java.io.Serializable;

import static tech.units.indriya.unit.Units.METRE;

/**
 * Created by nickrobison on 4/1/21
 */
public class NearestQuery implements Serializable {


    private static final long serialVersionUID = 42L;
    private static final Unit<Length> KM = MetricPrefix.KILO(METRE);
    private static final String NUMBER_EXN_FORMAT = "Cannot parse: `%s` as coordinate.";

    private final Quantity<Length> distance;
    private final Point point;


    private NearestQuery(Quantity<Length> distance, Point point) {
        this.distance = distance;
        this.point = point;
    }

    public Point getPoint() {
        return point;
    }

    public Quantity<Length> getDistance() {
        return distance;
    }

    public static NearestQuery fromToken(String param) {
        // FHIR defines the query as [latitude]|[longitude]|[distance]|[units]
        final String[] split = param.split("\\|");
        if (split.length < 2) {
            throw new IllegalArgumentException("Query must have lat/lon");
        }
        final double lon, lat;
        try {
            lon = Double.parseDouble(split[1]);
            lat = Double.parseDouble(split[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format(NUMBER_EXN_FORMAT, e.getMessage()));
        }
        final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
        final Point point = factory.createPoint(new Coordinate(lon, lat));

        if (split.length >= 4) {
            final double distance;
            try {
                distance = Double.parseDouble(split[2]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(String.format(NUMBER_EXN_FORMAT, e.getMessage()));
            }

            // Indriya doesn't have built-in support for english units (e.g. miles), so we need a hack
            if (split[3].equals("mi")) {
                return new NearestQuery(Quantities.getQuantity(distance, CLDR.MILE), point);
            }
            try {
                final Unit<Length> unit = SimpleUnitFormat.getInstance().parse(split[3]).asType(Length.class);
                return new NearestQuery(Quantities.getQuantity(distance, unit), point);
            } catch (MeasurementParseException e) {
                throw new IllegalArgumentException(String.format("Unsupported unit: %s", e.getParsedString()));
            }


        } else if (split.length == 3) {
            final double distance = Double.parseDouble(split[2]);
            return new NearestQuery(Quantities.getQuantity(distance, KM), point);
        }

        return new NearestQuery(Quantities.getQuantity(10, KM), point);
    }
}
