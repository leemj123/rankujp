package com.lee.rankujp.hotel.mvc.dto;

import com.lee.rankujp.hotel.infra.Hotel;
import lombok.Getter;

@Getter
public class ScoreResponse {
    private final long id;
    private final String thumbnailImg;
    private final String koName;
    private final int preferenceValue;
    private final double starRating;
    private final int rankuScore;

    public ScoreResponse(Hotel hotel) {
        this.id = hotel.getId();
        this.thumbnailImg = hotel.getThumbnailImg();
        this.koName = hotel.getKoName();
        this.preferenceValue =
                this.preferenceValueCalculator(hotel.getAverageAllScore(), hotel.getAverageCoupleScore(), hotel.getAverageSoloScore(), hotel.getAverageFamilyScore());
        this.starRating = hotel.getStarRating();
        this.rankuScore = hotel.getRankuScore();
    }
    private int preferenceValueCalculator(double abs, double acs, double ass, double afs) {
        double max = abs;
        int maxLabel = 1;

        if (acs > max) {
            max = acs;
            maxLabel = 2;
        }
        if (ass > max) {
            max = ass;
            maxLabel = 3;
        }
        if (afs > max) {
            maxLabel = 4;
        }
        return maxLabel;
    }
}
