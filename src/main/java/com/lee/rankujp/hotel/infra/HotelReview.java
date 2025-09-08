package com.lee.rankujp.hotel.infra;

import com.lee.rankujp.hotel.cumtom.ReviewBrand;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class HotelReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "hotel_review_list")
    private Hotel hotel;

    private ReviewBrand reviewBrand;

    private long reviewCount;
    private float allScore;

    private float businessScore;
    private float coupleScore;
    private float soloScore;
    private float familyScore;
    private float groupScore;

}
