package com.lee.rankujp.hotel.review.scrapper.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GooglePlaceReviewDto {
    private double rating;
    private int userRatingCount;
}
