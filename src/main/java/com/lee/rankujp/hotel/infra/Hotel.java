package com.lee.rankujp.hotel.infra;

import com.lee.rankujp.hotel.cumtom.PointLocation;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;
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
    private String keyword;

    //content
    @ManyToOne
    @JoinColumn(name = "hotel_city_id")
    private HotelCity hotelCity;

    private String enName;
    private String koName;
    private String jpName;

    private String address;
    private String zipcode;
    private double starRating;

    private int rankuScore;

    private double longitude;
    private double latitude;

    private long reviewNum;

    private String thumbnailImg;
    private String photo2;
    private String photo3;
    private String photo4;
    private String photo5;

//    //price
//    @OneToMany(mappedBy = "hotel",cascade = CascadeType.REMOVE)
//    private List<HotelPrice> priceList;

    private LocalDate bestStayDate;
    private double bestCrossedOutRate;
    private double bestDailyRate;
    private double bestSalePrecent;

    //review
    @OneToMany(mappedBy = "hotel",cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<HotelReview> hotelReviewList = new ArrayList<>();

    private double averageAllScore;
    private double averageBusinessScore;
    private double averageCoupleScore;
    private double averageSoloScore;
    private double averageFamilyScore;
    private double averageGroupScore;

    @Enumerated(EnumType.ORDINAL)
    private PointLocation pointLocation;

    private boolean isShow;

    public void averageScoreUpdate(long count ,double v, double v1, double v2, double v3, double v4, double v5) {
        this.reviewNum = count;
        this.averageAllScore = v;
        this.averageBusinessScore = v1;
        this.averageCoupleScore = v2;
        this.averageSoloScore = v3;
        this.averageFamilyScore = v4;
        this.averageGroupScore = v5;
    }

    public void rankuScoreUpdater (int r) {
        this.updateDateTime = LocalDateTime.now();
        this.rankuScore = r;
    }

    public void faUp() {
        double temp = this.averageFamilyScore / 2;
        this.averageFamilyScore = Math.floor(temp * 10) / 10.0;
    }

}
