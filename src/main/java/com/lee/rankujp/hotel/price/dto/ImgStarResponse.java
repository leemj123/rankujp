package com.lee.rankujp.hotel.price.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ImgStarResponse {
    private ErrorResponse error;
    private List<HotelApiInfo> results;

    @Data
    public static class HotelApiInfo {
        private String hotelName;
        private double starRating;
        private String imageURL;
    }
    @Data
    public static class ErrorResponse {
        private int id;
        private String message;

    }
}
