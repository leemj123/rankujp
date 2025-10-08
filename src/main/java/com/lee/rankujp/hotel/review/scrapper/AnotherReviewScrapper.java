package com.lee.rankujp.hotel.review.scrapper;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor

public class AnotherReviewScrapper {

    private final TripService tripService;
    private final JalanService jalanService;
    private final GooglePlaceService googlePlaceService;

//    @PostMapping("/odong/scrap/trip")
//    public void tripReview() {
//        tripService.startReviewScrap();
//    }

    @PostMapping("/odong/scrap/jalan")
    public void jalanReview() {
        jalanService.startReviewScrap();
    }
    @PostMapping("/odong/scrap/google")
    public void googleReview() {
        googlePlaceService.startReviewScrap();
    }
}
