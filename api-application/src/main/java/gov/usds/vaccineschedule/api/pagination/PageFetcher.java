package gov.usds.vaccineschedule.api.pagination;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.data.domain.Pageable;

import java.util.List;

@FunctionalInterface
public interface PageFetcher {
    List<IBaseResource> fetch(Pageable page);
}
