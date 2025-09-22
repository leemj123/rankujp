package com.lee.rankujp.hotel.infra;

import com.lee.rankujp.hotel.cumtom.ReviewBrand;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
public class HotelReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "hotel_review_list")
    private Hotel hotel;

    @Enumerated(EnumType.STRING)
    private ReviewBrand reviewBrand;

    private long reviewCount;
    private double allScore;

    private double businessScore;
    private double coupleScore;
    private double soloScore;
    private double familyScore;
    private double groupScore;

}
