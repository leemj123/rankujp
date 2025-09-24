package com.lee.rankujp.hotel.price;

import com.lee.rankujp.hotel.price.legacy.HotelInfoUpdater;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hotel")
@Slf4j
public class HotelPriceAPiController {

    private final HotelPriceService hotelPriceService;
    private final HotelInfoUpdater hotelInfoUpdater;

    @PostMapping("/price")
    public ResponseEntity<Map<String,String>> HotelPriceApiRequester() {

        Mono<Void> run = hotelPriceService.syncAllPriceWindowBatched()
                .doOnSubscribe(s -> log.info("[/agoda] sync started"))
                .doOnError(e -> log.error("[/agoda] sync failed", e))
                .doFinally(sig -> log.info("[/agoda] finally: {}", sig));

        run.subscribe();

        return ResponseEntity.accepted().body(Map.of("status","started"));
    }
    @PostMapping("/update")
    public void HotelImgAndStarApiRequester() {

        hotelInfoUpdater.imgAndStarUpdate();

    }
}
