package com.lee.rankujp.hotel.mvc.dto;

import lombok.Getter;

@Getter
public class HotelWithScore {
    private final long id;
    private final String thumbnailImg;
    private final String koName;
    private final int preferenceValue;
    private final double starRating;
    private final int rankuScore;

    public HotelWithScore(long id, String thumbnailImg, String koName, double starRating, int rankuScore, double abs, double acs, double ass, double afs) {
        this.id = id;
        this.thumbnailImg = thumbnailImg;
        this.koName = koName;
        this.preferenceValue = this.preferenceValueCalculator(abs,acs,ass,afs);
        this.starRating = starRating;
        this.rankuScore = rankuScore;
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
