package com.lee.rankujp.hotel.review;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Getter
public class AgodaReviewResponse {
    private Score score;

    @Data
    public static class Score {
        private String reviewPageUrl;
        private Long hotelId;
        private List<Demographic> demographics;
    }

    @Data
    public static class Demographic {
        private Long providerId;
        private Double score;
        private Double maxScore;
        private Integer count;
        private String name;
    }
}
