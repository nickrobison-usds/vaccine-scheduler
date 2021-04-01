package gov.usds.vaccineschedule.api.services;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import gov.usds.vaccineschedule.api.db.models.ScheduleEntity;
import gov.usds.vaccineschedule.api.db.models.SlotEntity;
import gov.usds.vaccineschedule.api.repositories.ScheduleRepository;
import gov.usds.vaccineschedule.api.repositories.SlotRepository;
import org.hl7.fhir.r4.model.Slot;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static gov.usds.vaccineschedule.api.db.models.Constants.ORIGINAL_ID_SYSTEM;
import static gov.usds.vaccineschedule.api.repositories.SlotRepository.forLocation;
import static gov.usds.vaccineschedule.api.repositories.SlotRepository.forLocationAndTime;

/**
 * Created by nickrobison on 3/29/21
 */
@Service
@Transactional(readOnly = true)
public class SlotService {

    private final ScheduleRepository scheduleRepository;
    private final SlotRepository repo;


    public SlotService(ScheduleRepository scheduleRepository, SlotRepository repo) {
        this.scheduleRepository = scheduleRepository;
        this.repo = repo;
    }

    public Collection<Slot> getSlots() {
        return StreamSupport
                .stream(this.repo.findAll().spliterator(), false)
                .map(SlotEntity::toFHIR)
                .collect(Collectors.toList());
    }

    public Collection<Slot> getSlotsForLocation(ReferenceParam idParam, DateRangeParam dateParam) {
        final UUID id = UUID.fromString(idParam.getIdPart());

        final Specification<SlotEntity> searchParams;
        if (dateParam.isEmpty()) {
            searchParams = forLocation(id);
        } else {
            searchParams = forLocationAndTime(id, dateParam);
        }

        return this.repo.findAll(searchParams)
                .stream()
                .map(SlotEntity::toFHIR)
                .collect(Collectors.toList());
    }

    @Transactional
    public Collection<Slot> addSlots(Collection<Slot> resources) {
        return resources
                .stream().map(this::addSlot)
                .map(SlotEntity::toFHIR)
                .collect(Collectors.toList());
    }

    @Transactional
    public SlotEntity addSlot(Slot resource) {
        final String scheduleRef = resource.getSchedule().getReference();

        final List<ScheduleEntity> schedule = StreamSupport.stream(scheduleRepository.findAll(ScheduleRepository.hasIdentifier(ORIGINAL_ID_SYSTEM, scheduleRef)).spliterator(), false).collect(Collectors.toList());
        if (schedule.isEmpty()) {
            throw new IllegalStateException("Cannot add to missing schedule");
        }

        final SlotEntity entity = SlotEntity.fromFHIR(schedule.get(0), resource);
        return repo.save(entity);
    }
}
