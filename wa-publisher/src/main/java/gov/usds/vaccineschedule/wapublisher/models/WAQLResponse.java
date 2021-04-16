package gov.usds.vaccineschedule.wapublisher.models;

import java.util.List;

/**
 * Created by nickrobison on 4/16/21
 */
public class WAQLResponse {

    private LocationResponse searchLocations;

    public LocationResponse getSearchLocations() {
        return searchLocations;
    }

    public static class LocationResponse {
        private List<WALocation> locations;

        public List<WALocation> getLocations() {
            return locations;
        }
    }
}
