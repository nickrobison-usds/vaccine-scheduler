package gov.usds.vaccineschedule.wapublisher.models;

import java.util.List;

/**
 * Created by nickrobison on 4/16/21
 */
public class LocationResponse {
    private List<WALocation> locations;
    private Pagination paging;

    public List<WALocation> getLocations() {
        return locations;
    }

    public Pagination getPaging() {
        return paging;
    }

    public static class Pagination {
        private int pageSize;
        private int pageNum;
        private int total;

        public int getPageSize() {
            return pageSize;
        }

        public int getPageNum() {
            return pageNum;
        }

        public int getTotal() {
            return total;
        }
    }
}
