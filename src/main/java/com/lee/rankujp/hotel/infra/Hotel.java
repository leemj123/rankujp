package com.lee.rankujp.hotel.infra;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
public class Hotel {
    @Id
    private Long id;

    private LocalDateTime updateDateTime;
    //seo
    private String title;
    private String description;
    //content
    @ManyToOne
    @JoinColumn(name = "hotel_city_id")
    private HotelCity hotelCity;

    private String enName;
    private String koName;

    private String address;
    private String zipcode;
    private String starRating;

    private double longitude;
    private double latitude;

    private long reviewNum;

    private String thumbnailImg;
    private String photo2;
    private String photo3;
    private String photo4;
    private String photo5;

    //review
    @OneToMany(mappedBy = "hotel")
    private List<HotelReview> hotelReviewList;

    private float averageAllScore;
    private float averageBusinessScore;
    private float averageCoupleScore;
    private float averageSoloScore;
    private float averageFamilyScore;
    private float groupScore;

}
