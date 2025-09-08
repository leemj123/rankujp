package com.lee.rankujp.hotel.review;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hotel/review")
public class HotelReviewController {

    private final HotelReviewService hotelReviewService;

    @PostMapping("/{id}")
    public void agodaReviewIdScraper( @PathVariable long id ) {

        hotelReviewService.idScrapper(id);
    }
}
