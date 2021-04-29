package gov.usds.vaccineschedule.api.models;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by nickrobison on 4/29/21
 */
public class SyncRequest implements Serializable {
    public static final long serialVersionUID = 42L;

    private final UUID syncSessionID;
    private final String url;

    public SyncRequest(UUID syncSessionID, String url) {
        this.syncSessionID = syncSessionID;
        this.url = url;
    }

    public UUID getSyncSessionID() {
        return syncSessionID;
    }

    public String getUrl() {
        return url;
    }
}
