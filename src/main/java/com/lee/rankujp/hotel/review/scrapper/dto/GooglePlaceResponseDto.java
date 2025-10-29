package com.lee.rankujp.hotel.review.scrapper.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GooglePlaceResponseDto {
    private List<GooglePlace> places;

    @Getter
    @Setter
    public static class GooglePlace {
        private String id;
    }
}
