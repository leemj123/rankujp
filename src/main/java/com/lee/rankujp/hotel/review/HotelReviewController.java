//package com.lee.rankujp.hotel.review;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import reactor.core.publisher.Mono;
//
//import java.util.Map;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/hotel/review")
//@Slf4j
//public class HotelReviewController {
//
//    private final HotelReviewService hotelReviewService;
//
//    @PostMapping("/agoda")
//    public ResponseEntity<Map<String,String>> agodaReviewIdScraper() {
//        Mono<Void> run = hotelReviewService.syncAllReviews()
//                .doOnSubscribe(s -> log.info("[/agoda] sync started"))
//                .doOnError(e -> log.error("[/agoda] sync failed", e))
//                .doOnSuccess(v -> log.info("[/agoda] sync completed"))
//                .doFinally(sig -> log.info("[/agoda] finally: {}", sig));
//
//        run.subscribe(); // ← 실제 실행 트리거
//
//        return ResponseEntity.accepted().body(Map.of("status","started"));
//
//    }
//}
