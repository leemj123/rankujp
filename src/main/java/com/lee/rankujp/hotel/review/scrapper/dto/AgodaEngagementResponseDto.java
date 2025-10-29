package com.lee.rankujp.hotel.review.scrapper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AgodaEngagementResponseDto {
    private DataWrapper data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataWrapper {
        private PropertyDetailsSearch propertyDetailsSearch;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PropertyDetailsSearch {
        private List<PropertyDetail> propertyDetails;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PropertyDetail {
        private Long propertyId;
        private ContentDetail contentDetail;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContentDetail {
        private ContentEngagement contentEngagement;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContentEngagement {
        // JSON shows empty string or "오늘 4회 예약됨" -> String is fine.
        private String peopleLooking;
        private String todayBooking;
    }

}
