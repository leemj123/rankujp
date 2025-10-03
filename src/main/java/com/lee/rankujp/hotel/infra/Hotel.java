package com.lee.rankujp.hotel.infra;

import com.lee.rankujp.hotel.cumtom.PointLocation;
import com.lee.rankujp.hotel.price.dto.ImgStarResponse;
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

    //price
    @OneToMany(mappedBy = "hotel",cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<HotelPrice> priceList;

    private LocalDate bestStayDate;
    private double bestCrossedOutRate;
    private double bestDailyRate;
    private double bestSailPrecent;

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

    public void averageScoreUpdate(double v, double v1, double v2, double v3, double v4, double v5) {
        this.averageAllScore = v;
        this.averageBusinessScore = v1;
        this.averageCoupleScore = v2;
        this.averageSoloScore = v3;
        this.averageFamilyScore = v4;
        this.averageGroupScore = v5;
    }
    public void titleUpdater (String t) {
        this.title = t;
    }
    public void descriptionUpdater (String d) {
        this.description = d;
    }
    public void keywordUpdater (String k) {
        this.keyword = k;
    }
    public void rankuScoreUpdater (int r) {
        this.updateDateTime = LocalDateTime.now();
        this.rankuScore = r;
    }

    public void beatScoreUpdate(LocalDate bsd, double bco, double bdr, double bsp) {
        this.bestStayDate = bsd;
        this.bestCrossedOutRate = bco;
        this.bestDailyRate = bdr;
        this.bestSailPrecent = bsp;
    }


    public void imgUpdate(ImgStarResponse.HotelApiInfo info) {
        this.thumbnailImg = info.getImageUrl();
    }

    public void enNameUpdate(ImgStarResponse.HotelApiInfo info) {
        this.enName = info.getHotelName();
    }
    public void jpNameUpdate(ImgStarResponse.HotelApiInfo info) {
        this.jpName = info.getHotelName();
    }
}
