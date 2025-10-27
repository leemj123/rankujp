package com.lee.rankujp.hotel.mvc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class HotelWithPrice {
    private final long id;
    private final String thumbnailImg;
    private final String koName;
    private final int preferenceValue;
    private final double starRating;
    private final int bestCrossedOutRate;
    private final int bestDailyRate;
    private final int bestSalePrecent;

    public HotelWithPrice(long id, String thumbnailImg, String koName, double starRating, double crossedOutRate, double dailyRate, double salePercent, double abs, double acs, double ass, double afs) {
        this.id = id;
        this.thumbnailImg = thumbnailImg;
        this.koName = koName;
        this.starRating = starRating;
        this.bestCrossedOutRate = (int)crossedOutRate;
        this.bestDailyRate = (int)dailyRate;
        this.bestSalePrecent = (int)salePercent;
        this.preferenceValue = this.preferenceValueCalculator(abs,acs,ass,afs);
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
