package com.lee.rankujp.hotel.price;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hotel/price")
@Slf4j
public class HotelPriceAPiController {

    private final HotelPriceService hotelPriceService;

    @PostMapping
    public List<AgodaPriceResponse.HotelApiInfo> HotelPriceApiRequester() {
        return hotelPriceService.getAgodaPrice();
    }
}
