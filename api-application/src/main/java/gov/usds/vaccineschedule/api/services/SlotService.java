package gov.usds.vaccineschedule.api.services;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import gov.usds.vaccineschedule.api.db.models.ScheduleEntity;
import gov.usds.vaccineschedule.api.db.models.SlotEntity;
import gov.usds.vaccineschedule.api.repositories.ScheduleRepository;
import gov.usds.vaccineschedule.api.repositories.SlotRepository;
import gov.usds.vaccineschedule.common.models.VaccineSlot;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static gov.usds.vaccineschedule.api.db.models.Constants.ORIGINAL_ID_SYSTEM;
import static gov.usds.vaccineschedule.api.repositories.SlotRepository.forLocation;
import static gov.usds.vaccineschedule.api.repositories.SlotRepository.forLocationAndTime;
import static gov.usds.vaccineschedule.api.repositories.SlotRepository.withIdentifier;

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

    public List<IBaseResource> findSlots(TokenParam identifier, Pageable page) {
        return this.repo.findAll(withIdentifier(identifier.getSystem(), identifier.getValue()), page)
                .stream().map(SlotEntity::toFHIR)
                .collect(Collectors.toList());
    }

    public List<IBaseResource> getSlots(Pageable page) {
        return StreamSupport
                .stream(this.repo.findAll(page).spliterator(), false)
                .map(SlotEntity::toFHIR)
                .collect(Collectors.toList());
    }

    public long getQueryCount() {
        return this.repo.count();
    }

    public List<IBaseResource> getSlotsForLocation(ReferenceParam idParam, @Nullable DateRangeParam dateParam, Pageable page) {
        final UUID id = UUID.fromString(idParam.getIdPart());

        final Specification<SlotEntity> searchParams;
        if (dateParam == null) {
            searchParams = forLocation(id);
        } else {
            searchParams = forLocationAndTime(id, dateParam);
        }

        return this.repo.findAll(searchParams, page)
                .stream()
                .map(SlotEntity::toFHIR)
                .collect(Collectors.toList());
    }

    @Transactional
    public Collection<VaccineSlot> addSlots(Collection<VaccineSlot> resources) {
        return resources
                .stream().map(this::addSlot)
                .map(SlotEntity::toFHIR)
                .collect(Collectors.toList());
    }

    @Transactional
    public SlotEntity addSlot(VaccineSlot resource) {
        final String scheduleRef = resource.getSchedule().getReference();

        final List<ScheduleEntity> schedule = StreamSupport.stream(scheduleRepository.findAll(ScheduleRepository.hasIdentifier(ORIGINAL_ID_SYSTEM, scheduleRef)).spliterator(), false).collect(Collectors.toList());
        if (schedule.isEmpty()) {
            throw new IllegalStateException("Cannot add to missing schedule");
        }

        final SlotEntity entity = SlotEntity.fromFHIR(schedule.get(0), resource);
        return repo.save(entity);
    }
}
