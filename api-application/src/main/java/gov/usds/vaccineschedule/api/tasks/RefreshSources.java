package gov.usds.vaccineschedule.api.tasks;

import gov.usds.vaccineschedule.api.services.SourceFetchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

import javax.ws.rs.InternalServerErrorException;

/**
 * Created by nickrobison on 3/25/21
 */
@Endpoint(id = "refresh")
@Component
public class RefreshSources {

    private static final Logger logger = LoggerFactory.getLogger(RefreshSources.class);

    private final SourceFetchService service;

    public RefreshSources(SourceFetchService service) {
        this.service = service;
    }

    @WriteOperation
    public void refreshSources() {
        logger.info("Refreshing data from sources");
        try {
            service.refreshSources();
        } catch (Exception e) {
            throw new InternalServerErrorException(e);
        }

    }
}
