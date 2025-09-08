package com.lee.rankujp.hotel.controller;

import com.lee.rankujp.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/agoda/api")
public class HotelController {

    private final HotelService hotelService;

    /*todo*
    starRating,
    reviewScore,
    reviewCount,
    dailyRate,
    crossedOutRate,
     */
    @PostMapping("/syn")
    public void allSyncer () {
        hotelService.allSyncer();
    }


}
