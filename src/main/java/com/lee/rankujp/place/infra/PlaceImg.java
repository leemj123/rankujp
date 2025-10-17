package com.lee.rankujp.place.infra;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class PlaceImg {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Column(length = 1000)
    private String name;
    private int widthPx;
    private int heightPx;
    //AuthorAttribution
    private String authorDisplayName;
    private String googleMapsUri;

    private String thumbnailUri;


//    public PlaceImg(GoogleRestaurantResponse.Photo photo) {
//        this.name = photo.getName();
//        this.widthPx = photo.getWidthPx();
//        this.heightPx = photo.getHeightPx();
//        this.authorDisplayName = photo.getAuthorAttributions() != null ?
//                photo.getAuthorAttributions().get(0).getDisplayName() : "none";
//        this.googleMapsUri = photo.getGoogleMapsUri();
//    }
    void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public void thumbnailUpdate(String s3Url) {
        this.thumbnailUri = s3Url;
    }
}
