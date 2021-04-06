package gov.usds.vaccineschedule.publisher.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import gov.usds.vaccineschedule.publisher.respositories.LocationRepository;
import gov.usds.vaccineschedule.publisher.respositories.ScheduleRepository;
import gov.usds.vaccineschedule.publisher.respositories.SlotRepository;
import org.hl7.fhir.r4.model.BaseResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by nickrobison on 3/25/21
 */
@RestController
@RequestMapping("/data")
public class DataProvider {
    private final IParser parser;

    private final ConcurrentMap<String, Supplier<List<? extends BaseResource>>> repos = new ConcurrentHashMap<>();

    public DataProvider(FhirContext ctx, LocationRepository lRepo, ScheduleRepository sRepo, SlotRepository slRepo) {
        this.parser = ctx.newJsonParser();

        repos.put("test-location", lRepo::getAll);
        repos.put("test-schedule", sRepo::getAll);
        repos.put("test-slot", slRepo::getAll);
    }

    @GetMapping(value = "/{id}.ndjson", produces = "application/fhir+ndjson", consumes = {"application/fhir+ndjson", "application/ndjson"})
    public ResponseEntity<String> getFile(@PathVariable String id) {

        if (!repos.containsKey(id)) {
            throw new NotFoundException(String.format("Cannot find file with id: %s", id));
        }

        final String response = repos.get(id).get()
                .stream()
                .map(parser::encodeResourceToString)
                .collect(Collectors.joining("\n"));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
