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
    private final int salePercent;
    private final LocalDateTime updatedAt;
    private final boolean isWeekend;
    private final String link;

    public HotelPriceResponse(HotelPrice hp, long id, String asq) {
        this.stayDate = hp.getStayDate();
        this.crossedOutRate = (int) hp.getCrossedOutRate();
        this.dailyRate = (int) hp.getDailyRate();
        this.salePercent = (int) hp.getSalePercent();
        this.updatedAt = hp.getUpdatedAt();
        this.isWeekend = hp.isWeekend();
        this.link = "https://www.agoda.com/partners/partnersearch.aspx" +
                "?pcs=1" +
                "&cid=1950715" +
                "&hl=ko-kr" +
                "&hid="+ id
                +"&checkin="+ hp.getStayDate()
                +"&checkout="+ hp.getStayDate().plusDays(2)
                +"&currency=JPY"
                +"&NumberofAdults=2&NumberofChildren=0&Rooms=1&pcs=6";
    }
}
//https://www.agoda.com/ko-kr/partners/partnersearch.aspx?cid=1911730&hid=38094586&currency=JPY&checkin=2025-11-02&checkout=2025-11-04&NumberofAdults=2&NumberofChildren=0&Rooms=1&pcs=6