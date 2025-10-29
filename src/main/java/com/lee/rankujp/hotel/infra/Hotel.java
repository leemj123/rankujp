package com.lee.rankujp.hotel.infra;

import com.lee.rankujp.big.AgodaKo;
import com.lee.rankujp.hotel.cumtom.PointLocation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
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

    public Hotel(AgodaKo agodaKo, HotelCity hotelCity) {
        this.id = agodaKo.getHotel_id();
        this.updateDateTime = LocalDateTime.now();
        this.title = null;
        this.description = agodaKo.getOverview();
        this.keyword = null;
        this.hotelCity = hotelCity;
        this.enName = agodaKo.getHotel_name();
        this.koName = agodaKo.getHotel_translated_name();
        this.jpName = null;
        this.address = agodaKo.getAddressline1() + agodaKo.getAddressline2();
        this.zipcode = agodaKo.getZipcode();
        this.starRating = Double.parseDouble(agodaKo.getStar_rating());
        this.rankuScore = 0;
        this.longitude = Double.parseDouble(agodaKo.getLongitude());
        this.latitude = Double.parseDouble(agodaKo.getLatitude());
        this.reviewNum = Long.parseLong(agodaKo.getNumber_of_reviews());
        this.thumbnailImg = agodaKo.getPhoto1();
        this.photo2 = agodaKo.getPhoto2();
        this.photo3 = agodaKo.getPhoto3();
        this.photo4 = agodaKo.getPhoto4();
        this.photo5 = agodaKo.getPhoto5();
        this.bestStayDate = null;
        this.bestCrossedOutRate = 0;
        this.bestDailyRate = 0;
        this.bestSalePrecent = 0;
        this.averageAllScore = 0;
        this.averageBusinessScore = 0;
        this.averageCoupleScore = 0;
        this.averageSoloScore = 0;
        this.averageFamilyScore = 0;
        this.averageGroupScore = 0;
        this.pointLocation = null;
        this.isShow = false;
    }
}
