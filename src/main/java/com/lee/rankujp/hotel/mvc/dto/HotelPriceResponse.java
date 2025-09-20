package com.lee.rankujp.hotel.mvc.dto;

import com.lee.rankujp.hotel.infra.HotelPrice;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class HotelPriceResponse {
    private final LocalDate stayDate;
    private final int crossedOutRate;
    private final int dailyRate;
    private final int sailPercent;
    private final LocalDateTime updatedAt;
    private final boolean isWeekend;
    private final String link;

    public HotelPriceResponse(HotelPrice hp) {
        this.stayDate = hp.getStayDate();
        this.crossedOutRate = (int) hp.getCrossedOutRate();
        this.dailyRate = (int) hp.getDailyRate();
        this.sailPercent = (int) hp.getSailPercent();
        this.updatedAt = hp.getUpdatedAt();
        this.isWeekend = hp.isWeekend();
        this.link = "test";
    }
}
