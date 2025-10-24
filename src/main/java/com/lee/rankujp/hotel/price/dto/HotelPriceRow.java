package com.lee.rankujp.hotel.price.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class HotelPriceRow {
    private Long hotelId;
    private LocalDate stayDate;
    private double dailyRate;
    private double crossedOutRate;
    private double salePercent;
    private boolean isWeekend;

    public HotelPriceRow(Long hotelId, LocalDate stayDate, double dailyRate, double crossedOutRate, double salePercent) {
        this.hotelId = hotelId;
        this.stayDate = stayDate;
        this.dailyRate = dailyRate;
        this.crossedOutRate = crossedOutRate;
        this.salePercent = salePercent;
        this.isWeekend = judgeWeekend(stayDate);
    }
    private static boolean judgeWeekend(LocalDate day) {
        var dow = day.getDayOfWeek();
        return dow == java.time.DayOfWeek.SATURDAY || dow == java.time.DayOfWeek.SUNDAY;
    }

}
