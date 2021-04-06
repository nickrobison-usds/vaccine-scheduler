package gov.usds.vaccineschedule.publisher.respositories;

import org.hl7.fhir.r4.model.Slot;

import java.util.List;

public interface SlotRepository {

    List<Slot> getAll();
}
