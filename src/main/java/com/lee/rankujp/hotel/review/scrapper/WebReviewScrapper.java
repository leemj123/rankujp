package com.lee.rankujp.hotel.review.scrapper;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WebReviewScrapper {

    private final TripService tripService;
    private final JalanService jalanService;

//    @PostMapping("/odong/scrap/trip")
//    public void tripReview() {
//        tripService.startReviewScrap();
//    }

    @PostMapping("/odong/scrap/jalan")
    public void jalanReview() {
        jalanService.startReviewScrap();
    }
}
