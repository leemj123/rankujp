package com.lee.rankujp.place.infra;

import com.lee.rankujp.hotel.cumtom.PointLocation;
import com.lee.rankujp.hotel.infra.HotelCity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String googlePlaceId;
    private String internationalPhoneNumber;
    private String formattedAddress;

    @ManyToOne
    @JoinColumn(name = "hotel_city_id")
    private HotelCity hotelCity;
    //Location
    private double latitude;
    private double longitude;

    private double rating;
    private String googleMapsUri;
    private String websiteUri;
    private long userRatingCount;

    //displayName
    private String title;
    private String titleLanguageCode;


    //PrimaryTypeDisplayName
    private String primaryText;
    private String primaryLanguageCode;


    private String primaryType;
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaceImg> placeImgList = new ArrayList<>();

    //priceRange
    private String strCurrencyCode;
    private String strUnits;
    private String endCurrencyCode;
    private String endUnits;

    //reviewSummary
    private String reviewText;
    private String reviewLanguageCode;

    private PointLocation pointLocation;

//    public Restaurant(GoogleRestaurantResponse grr) {
//        this.googlePlaceId = grr.getId();
//        this.internationalPhoneNumber = grr.getInternationalPhoneNumber();
//        this.formattedAddress = grr.getFormattedAddress();
//        this.longitude = grr.getLocation().getLongitude();
//        this.latitude = grr.getLocation().getLatitude();
//        this.rating = grr.getRating();
//        this.googleMapsUri = grr.getGoogleMapsUri();
//        this.websiteUri = grr.getWebsiteUri();
//        this.userRatingCount = grr.getUserRatingCount();
//        this.title = grr.getDisplayName().getText();
//        this.titleLanguageCode = grr.getDisplayName().getLanguageCode();
//        this.primaryText = grr.getPrimaryTypeDisplayName().getText();
//        this.primaryLanguageCode = grr.getPrimaryTypeDisplayName().getLanguageCode();
//        this.primaryType = grr.getPrimaryType();
//
//        if (grr.getPriceRange() != null) {
//            if (grr.getPriceRange().getStartPrice() != null) {
//                this.strCurrencyCode =  grr.getPriceRange().getStartPrice().getCurrencyCode();
//                this.strUnits = grr.getPriceRange().getStartPrice().getUnits();
//            }
//            if (grr.getPriceRange().getEndPrice() != null) {
//                this.endCurrencyCode = grr.getPriceRange().getEndPrice().getCurrencyCode();
//                this.endUnits =  grr.getPriceRange().getEndPrice().getUnits();
//            }
//
//        }
//        if (grr.getReviewSummary() != null) {
//            this.reviewText = grr.getReviewSummary().getText().getText();
//            this.reviewLanguageCode = grr.getReviewSummary().getText().getLanguageCode();
//        }
//
//    }

    public void addImage(PlaceImg img) {
        placeImgList.add(img);
        img.setRestaurant(this);
    }
}
