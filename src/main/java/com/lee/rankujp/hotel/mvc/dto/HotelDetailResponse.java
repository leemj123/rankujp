package com.lee.rankujp.hotel.mvc.dto;

import com.lee.rankujp.hotel.cumtom.ReviewBrand;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class HotelDetailResponse {

    private long id;
    private LocalDateTime updateDate;
    private String title;
    private String description;
    private String keyword;

    private String koName;
    private String jpName;
    private String enName;

    private String stateName;
    private long stateId;

    private int rankuScore;

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

    private LocalDate bestStayDate;
    private int bestCrossedOutRate;
    private int bestDailyRate;
    private int bestSailPrecent;

    private String bestLink;

    List<HotelPriceResponse> weekdayPriceList;
    List<HotelPriceResponse> weekendPriceList;

    private int preferenceValue;

    private int averageAllScore;
    private int averageBusinessScore;
    private int averageCoupleScore;
    private int averageSoloScore;
    private int averageFamilyScore;

    Map<ReviewBrand,HotelReviewResponse> brandReviewMap;
}
