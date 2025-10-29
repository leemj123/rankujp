package com.lee.rankujp.big;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class AgodaJa {
    @Id
    private long hotel_id;
    private String chain_id;
    private String chain_name;
    private String brand_id;
    private String brand_name;
    private String hotel_name;
    private String hotel_formerly_name;
    private String hotel_translated_name;
    private String addressline1;
    private String addressline2;
    private String zipcode;
    private String city;
    private String state;
    private String country;
    private String countryisocode;
    private String star_rating;
    private String longitude;
    private String latitude;
    private String url;
    private String checkin;
    private String checkout;
    private String numberrooms;
    private String numberfloors;
    private String yearopened;
    private String yearrenovated;
    private String photo1;
    private String photo2;
    private String photo3;
    private String photo4;
    private String photo5;
    private String overview;
    private String rates_from;
    private long continent_id;
    private String continent_name;
    private long city_id;
    private long country_id;
    private String number_of_reviews;
    private String rating_average;
    private String rates_currency;

}
