package com.lee.rankujp.hotel.mvc.dto;

import com.lee.rankujp.hotel.cumtom.ReviewBrand;
import com.lee.rankujp.hotel.infra.HotelReview;
import lombok.Getter;

@Getter
public class HotelReviewResponse {

    private final ReviewBrand reviewBrand;
    private final int allScore;
    private final long count;

    public HotelReviewResponse(HotelReview hr) {
        this.reviewBrand = hr.getReviewBrand();
        this.allScore =  (int) (hr.getAllScore() * 10);
        this.count = hr.getReviewCount();
    }
}
