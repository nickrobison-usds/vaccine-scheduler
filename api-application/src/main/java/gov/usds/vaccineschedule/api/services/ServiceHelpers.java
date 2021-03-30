package gov.usds.vaccineschedule.api.services;

import gov.usds.vaccineschedule.api.db.models.Flammable;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by nickrobison on 3/30/21
 */
public class ServiceHelpers {

    /**
     * Helper function to convert an {@link Iterable} (returned by JPA repos) to a collection of FHIR Resources of type {@link R}
     * The returned {@link Collection} is implemented as an {@link java.util.ArrayList}
     *
     * @param supplier  - {@link Supplier} which return an {@link Iterable} of {@link C}
     * @param converter - {@link Function} which takes an entity {@link C} and returns a FHIR resource {@link R}
     * @param <R>       - {@link R} FHIR resource extending {@link IBaseResource}
     * @param <C>       - {@link C} data entity implementing {@link Flammable} which implies we can go to and from FHIR resources
     * @return - {@link Collection} of FHIR Resource {@link C}
     */
    public static <R extends IBaseResource, C extends Flammable<R>> Collection<R> fromIterable(Supplier<Iterable<C>> supplier, Function<C, R> converter) {
        return StreamSupport
                .stream(supplier.get().spliterator(), false)
                .map(converter)
                .collect(Collectors.toList());
    }
}
