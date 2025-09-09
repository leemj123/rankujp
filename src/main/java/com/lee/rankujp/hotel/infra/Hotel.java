package com.lee.rankujp.hotel.infra;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private List<HotelReview> hotelReviewList = new ArrayList<>();

    private double averageAllScore;
    private double averageBusinessScore;
    private double averageCoupleScore;
    private double averageSoloScore;
    private double averageFamilyScore;
    private double averageGroupScore;


    public void averageScoreUpdate(double v, double v1, double v2, double v3, double v4, double v5) {
        this.averageAllScore = v;
        this.averageBusinessScore = v1;
        this.averageCoupleScore = v2;
        this.averageSoloScore = v3;
        this.averageFamilyScore = v4;
        this.averageGroupScore = v5;
    }
}
