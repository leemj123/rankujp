package com.lee.rankujp.hotel.review.scrapper;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AnotherReviewScrapper {

    private final TripService tripService;

//    @PostMapping("/odong/scrap/trip")
//    public void tripReview() {
//        tripService.startReviewScrap();
//    }
}
