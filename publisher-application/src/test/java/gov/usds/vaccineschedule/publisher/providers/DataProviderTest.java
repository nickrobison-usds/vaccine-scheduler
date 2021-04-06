package gov.usds.vaccineschedule.publisher.providers;

import gov.usds.vaccineschedule.publisher.utils.BaseApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static gov.usds.vaccineschedule.publisher.config.Constants.FHIR_NDJSON;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by nickrobison on 4/6/21
 */
public class DataProviderTest extends BaseApplicationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DataProvider provider;

    @Test
    void contextLoads() {
        assertNotNull(provider, "Provider should load");
    }

    @Test
    void acceptsFHIRNDJSON() throws Exception {
        final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/data/test-schedule.ndjson")
                .accept(FHIR_NDJSON);

        this.mockMvc
                .perform(builder)
                .andExpect(status().isOk());
    }

    @Test
    void rejectsOtherMediaTypes() throws Exception {
        final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/data/test-schedule.ndjson")
                .accept(MediaType.APPLICATION_JSON);

        this.mockMvc
                .perform(builder)
                .andExpect(status().isNotAcceptable());
    }
}
