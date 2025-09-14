package com.lee.rankujp.hotel.price.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class HotelPriceRow {
    private LocalDate stayDate;
    private double dailyRate;
    private double crossedOutRate;
    private double sailPercent;
    private String imageURL;
    private boolean isWeekend;

    public HotelPriceRow(LocalDate stayDate, double dailyRate, double crossedOutRate, double sailPercent, String imageURL) {
        this.stayDate = stayDate;
        this.dailyRate = dailyRate;
        this.crossedOutRate = crossedOutRate;
        this.sailPercent = sailPercent;
        this.imageURL = imageURL;
        this.isWeekend = judgeWeekend(stayDate);
    }
    private static boolean judgeWeekend(LocalDate day) {
        var dow = day.getDayOfWeek();
        return dow == java.time.DayOfWeek.SATURDAY || dow == java.time.DayOfWeek.SUNDAY;
    }

}
