package gov.usds.vaccineschedule.api.helpers;

public interface ContextCloser extends AutoCloseable {
    void close();
}
