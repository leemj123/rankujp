package com.lee.rankujp.hotel.mvc.dto;

import com.lee.rankujp.hotel.infra.Hotel;
import lombok.Getter;

@Getter
public class PremiumResponse {
    private final long id;
    private final String thumbnailImg;
    private final String koName;
    private final int preferenceValue;
    private final double starRating;
    private final int bestCrossedOutRate;
    private final int bestDailyRate;
    private final int bestSalePrecent;

    public PremiumResponse(Hotel hotel) {
        this.id = hotel.getId();
        this.thumbnailImg = hotel.getThumbnailImg();
        this.koName = hotel.getKoName();
        this.preferenceValue =
                this.preferenceValueCalculator(hotel.getAverageBusinessScore(), hotel.getAverageCoupleScore(), hotel.getAverageSoloScore(), hotel.getAverageFamilyScore());
        this.starRating = hotel.getStarRating();
        this.bestCrossedOutRate = (int) hotel.getBestCrossedOutRate();
        this.bestDailyRate = (int) hotel.getBestDailyRate();
        this.bestSalePrecent = (int) hotel.getBestSalePrecent();
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
