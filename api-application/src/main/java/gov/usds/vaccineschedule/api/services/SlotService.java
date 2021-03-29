package gov.usds.vaccineschedule.api.services;

import gov.usds.vaccineschedule.api.db.models.ScheduleEntity;
import gov.usds.vaccineschedule.api.db.models.SlotEntity;
import gov.usds.vaccineschedule.api.repositories.ScheduleRepository;
import gov.usds.vaccineschedule.api.repositories.SlotRepository;
import org.hl7.fhir.r4.model.Slot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static gov.usds.vaccineschedule.api.db.models.Constants.ORIGINAL_ID_SYSTEM;

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

    @Transactional
    public SlotEntity addSlot(Slot resource) {
        final String scheduleRef = resource.getSchedule().getReference();

        final List<ScheduleEntity> schedules = scheduleRepository.findAll(ScheduleRepository.hasIdentifier(ORIGINAL_ID_SYSTEM, scheduleRef));
        if (schedules.isEmpty()) {
            throw new IllegalStateException("Cannot add to missing schedule");
        }

        final SlotEntity entity = SlotEntity.fromFHIR(schedules.get(0), resource);
        return repo.save(entity);
    }
}
