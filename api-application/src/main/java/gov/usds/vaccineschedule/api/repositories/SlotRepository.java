package gov.usds.vaccineschedule.api.repositories;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ParamPrefixEnum;
import gov.usds.vaccineschedule.api.db.models.LocationEntity;
import gov.usds.vaccineschedule.api.db.models.LocationEntity_;
import gov.usds.vaccineschedule.api.db.models.ScheduleEntity;
import gov.usds.vaccineschedule.api.db.models.ScheduleEntity_;
import gov.usds.vaccineschedule.api.db.models.SlotEntity;
import gov.usds.vaccineschedule.api.db.models.SlotEntity_;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.SingularAttribute;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

@Repository
public interface SlotRepository extends CrudRepository<SlotEntity, UUID> {

    List<SlotEntity> findAll(Specification<SlotEntity> searchSpec);

    static Specification<SlotEntity> forLocation(UUID locationId) {
        return (root, cq, cb) -> {
            final Join<SlotEntity, ScheduleEntity> scheduleJoin = root.join(SlotEntity_.schedule);

            final Join<ScheduleEntity, LocationEntity> locationJoin = scheduleJoin.join(ScheduleEntity_.location);

            return cb.equal(locationJoin.get(LocationEntity_.internalId), locationId);
        };
    }

    static Specification<SlotEntity> withinTimeRange(DateRangeParam param) {
        return (root, cq, cb) -> {

            List<Predicate> predicates = new ArrayList<>();
            final SingularAttribute<SlotEntity, OffsetDateTime> startExpression = SlotEntity_.startTime;
            // Lower bound?
            if (param.getLowerBound() != null) {
                final OffsetDateTime start = param.getLowerBoundAsInstant().toInstant().atOffset(ZoneOffset.UTC);
                final BiFunction<Expression<? extends OffsetDateTime>, OffsetDateTime, Predicate> lowerPredicate = buildPredicate(OffsetDateTime.class, cb, param.getLowerBound().getPrefix());
                predicates.add(lowerPredicate.apply(root.get(startExpression), start));
            }
            // Upper bound?
            if (param.getUpperBound() != null) {
                final OffsetDateTime end = param.getUpperBoundAsInstant().toInstant().atOffset(ZoneOffset.UTC);
                final BiFunction<Expression<? extends OffsetDateTime>, OffsetDateTime, Predicate> upperPredicate = buildPredicate(OffsetDateTime.class, cb, param.getUpperBound().getPrefix());
                predicates.add(upperPredicate.apply(root.get(startExpression), end));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    static Specification<SlotEntity> forLocationAndTime(UUID locationID, DateRangeParam dateRange) {
        return Specification.where(forLocation(locationID)).and(withinTimeRange(dateRange));
    }

    private static <Y extends Comparable<? super Y>> BiFunction<Expression<? extends Y>, Y, Predicate> buildPredicate(Class<Y> clazz, CriteriaBuilder cb, ParamPrefixEnum value) {
        switch (value) {
            case GREATERTHAN:
                return cb::greaterThan;
            case GREATERTHAN_OR_EQUALS:
                return cb::greaterThanOrEqualTo;
            case LESSTHAN:
                return cb::lessThan;
            case LESSTHAN_OR_EQUALS:
                return cb::lessThanOrEqualTo;
            case EQUAL:
                return cb::equal;
            default:
                throw new IllegalStateException(String.format("Unknown conditional: %s", value));
        }
    }
}
