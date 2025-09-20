package com.lee.rankujp.hotel.mvc.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class HotelDetailResponse {

    private long id;
    private LocalDate updateDate;
    private String title;
    private String description;

    private String koName;

    private String address;
    private String zipcode;
    private double starRating;

    private double latitude;
    private double longitude;

    private String thumbnailImg;
    private String photo2;
    private String photo3;
    private String photo4;
    private String photo5;

    private int bestCrossedOutRate;
    private int bestDailyRate;
    private int bestSailPrecent;

    List<HotelPriceResponse> priceList;

    private int preferenceValue;

    private int averageAllScore;
    private int averageBusinessScore;
    private int averageCoupleScore;
    private int averageSoloScore;
    private int averageFamilyScore;

    List<HotelReviewResponse> brandReviewList;
}
