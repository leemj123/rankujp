package com.lee.rankujp.place.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GooglePlaceRequest {
    List<String> includedPrimaryTypes = new ArrayList<>();
    List<String> excludedPrimaryTypes = new ArrayList<>();
    int maxResultCount;
    String languageCode;
    String regionCode;
    LocationRestriction locationRestriction;

    @Data
    public static class LocationRestriction {
        LocationCircle circle;


        public LocationRestriction(double latitude, double longitude) {
            this.circle = new LocationCircle(latitude, longitude);
        }
        @Data
        public static class LocationCircle {
            LocationCenter center;
            int radius;

            public LocationCircle(double latitude, double longitude) {
                center = new LocationCenter(latitude, longitude);
                radius = 800;
            }
            @Data
            public static class LocationCenter {
                double latitude;
                double longitude;

                public LocationCenter(double latitude, double longitude) {
                    this.latitude = latitude;
                    this.longitude = longitude;
                }
            }
        }
    }

    public GooglePlaceRequest(double latitude, double longitude) {
        this.includedPrimaryTypes.addAll(
                List.of(
                        "japanese_restaurant",
                        "ramen_restaurant",
                        "sushi_restaurant",
                        "barbecue_restaurant",
                        "seafood_restaurant"
                ));
        this.excludedPrimaryTypes.addAll(
                List.of(
                        "cafeteria",
                        "deli",
                        "dessert_restaurant",
                        "dessert_shop",
                        "korean_restaurant",
                        "american_restaurant",
                        "pizza_restaurant",
                        "sandwich_shop",
                        "brazilian_restaurant"
                ));
        this.maxResultCount = 20;
        this.languageCode = "ko";
        this.regionCode = "JP";
        this.locationRestriction = new LocationRestriction(latitude, longitude);
    }


    public void updateRadius (int radius) {
        this.locationRestriction.circle.radius = radius;
    }
    public void updateLocation(double latitude, double longitude) {
        this.locationRestriction.circle.center.latitude = latitude;
        this.locationRestriction.circle.center.longitude = longitude;
    }
}

