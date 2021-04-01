package gov.usds.vaccineschedule.common.helpers;

import ca.uhn.fhir.parser.IParser;
import cov.usds.vaccineschedule.common.models.VaccineSlot;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by nickrobison on 3/25/21
 */
public class NDJSONToFHIR {

    private static final Logger logger = LoggerFactory.getLogger(NDJSONToFHIR.class);

    private final IParser parser;

    public NDJSONToFHIR(IParser parser) {
        this.parser = parser;
        this.parser.setPreferTypes(Collections.singletonList(VaccineSlot.class));
    }

    /**
     * Split the given {@link InputStream} by lines and convert each line to a FHIR resource
     * Note: It is the requirement of the caller to actually close and cleanup the input stream, we only take care of our own allocations
     *
     * @param stream - {@link InputStream} to split into lines
     * @return - {@link List} of {@link IBaseResource} parsed from NDJSON lines
     */
    public List<IBaseResource> inputStreamToResource(InputStream stream) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return bufferedReader.lines()
                    .map(parser::parseResource)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e); // Don't throw runtime exceptions here
        }
    }

    public <T extends IBaseResource> List<T> inputStreamToTypedResource(Class<T> clazz, InputStream stream) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return bufferedReader.lines()
                    .map(r -> parser.parseResource(clazz, r))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e); // Don't throw runtime exceptions here
        }
    }
}
