package gov.usds.vaccineschedule.publisher.models;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Created by nickrobison on 3/25/21
 */
public class PublishResponse {

    private OffsetDateTime transactionTime;
    private String request;
    private List<OutputEntry> output;
    private List<String> error;

    public PublishResponse() {
        // Jackson required
    }

    public OffsetDateTime getTransactionTime() {
        return transactionTime;
    }

    public String getRequest() {
        return request;
    }

    public List<OutputEntry> getOutput() {
        return output;
    }

    public List<String> getError() {
        return error;
    }

    public void setTransactionTime(OffsetDateTime transactionTime) {
        this.transactionTime = transactionTime;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public void setOutput(List<OutputEntry> output) {
        this.output = output;
    }

    public void setError(List<String> error) {
        this.error = error;
    }

    public static class OutputEntry {
        private String type;
        private String url;

        public OutputEntry(String type, String url) {
            this.type = type;
            this.url = url;
        }

        public String getType() {
            return type;
        }

        public String getUrl() {
            return url;
        }
    }
}
