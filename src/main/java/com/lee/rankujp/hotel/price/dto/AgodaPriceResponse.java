package com.lee.rankujp.hotel.price.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AgodaPriceResponse {

    private List<HotelApiInfo> results;

    @Data
    public static class HotelApiInfo {
        private long hotelId;
        private double dailyRate;
        private double crossedOutRate;
        private double discountPercentage;
        private String imageURL;
    }
}
