package com.lee.rankujp.place.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RestaurantResponseDto {
    private long id;
    private String thumbnailUri;
    private String authorDisplayName;
    private String authorGoogleMapsUri;
    private String title;
    private String titleLanguageCode;
    private int district;
    private String primaryText;
    private double rating;
    private long userRatingCount;
    private String googleMapsUri;
}
