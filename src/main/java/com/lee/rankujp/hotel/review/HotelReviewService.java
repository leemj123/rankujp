//package com.lee.rankujp.hotel.review;
//
//import com.lee.rankujp.hotel.cumtom.ReviewBrand;
//import com.lee.rankujp.hotel.infra.Hotel;
//import com.lee.rankujp.hotel.infra.HotelReview;
//import com.lee.rankujp.hotel.infra.QHotel;
//import com.lee.rankujp.hotel.repo.HotelRepo;
//import com.lee.rankujp.hotel.repo.HotelReviewRepo;
//import com.querydsl.jpa.impl.JPAQueryFactory;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.support.TransactionTemplate;
//import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.web.reactive.function.client.WebClientResponseException;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import reactor.core.scheduler.Schedulers;
//import reactor.netty.http.client.PrematureCloseException;
//import reactor.util.retry.Retry;
//
//import java.net.ConnectException;
//import java.net.UnknownHostException;
//import java.time.Duration;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ThreadLocalRandom;
//import java.util.concurrent.TimeoutException;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class HotelReviewService {
//
//    private final WebClient agodaWebClient;
//    private final HotelRepo hotelRepo;
//    private final HotelReviewRepo hotelReviewRepo;
//    private final TransactionTemplate tx;
//
//    private static final int CONCURRENCY = 8;
//    private static final int BATCH = 1000;
//    private static final Duration BASE = Duration.ofMillis(100);
//    private static final long JITTER_MS = 200;
//
//    private final JPAQueryFactory jpaQueryFactory;
//    private final QHotel qHotel = QHotel.hotel;
//
////    @Transactional
////    public void agodaReviewMapper() {
////
////        Hotel hotel = hotelRepo.findById(59L).orElseThrow();
////        AgodaReviewResponse review = this.agodaIdScrapper(hotel.getId()).block();
////
////        if (review == null) return;
////
////        List<AgodaReviewResponse.Demographic> reviewType = review.getScore().getDemographics();
////        List<HotelReview> hotelReviewList = new ArrayList<>();
////        {
////            List<AgodaReviewResponse.Demographic> agodaList = new ArrayList<>();
////            List<AgodaReviewResponse.Demographic> bookingList = new ArrayList<>();
////
////            for (AgodaReviewResponse.Demographic d : reviewType) {
////
////                if (d.getProviderId() == 332) agodaList.add(d);
////                else if (d.getProviderId() == 3038) bookingList.add(d);
////
////            }
////
////            hotelReviewList.add(this.reviewSeparator(agodaList, hotel, ReviewBrand.AGODA));
////            hotelReviewList.add(this.reviewSeparator(bookingList, hotel, ReviewBrand.BOOKINGDOTCOM));
////        }
////
////        hotelReviewRepo.saveAll(hotelReviewList);
////        hotel.getHotelReviewList().addAll(hotelReviewList);
////        this.averageScoreCalculator(hotel);
////
////    }
//    private static Duration jittered() {
//        long j = ThreadLocalRandom.current().nextLong(-JITTER_MS, JITTER_MS + 1);
//        return BASE.plusMillis(j);
//    }
//    public Mono<Void> syncAllReviews() {
//        return idBatches()
//                .concatMap(ids -> {
//                    return Flux.fromIterable(ids)
//                            .concatMap(id -> Mono.delay(jittered()).thenReturn(id))
//                            .flatMap(this::processOne, CONCURRENCY);
//                },1)
//                .then();
//    }
//    public List<Long> fetchIdsDSL (int page) {
//        long offset = (long) page * BATCH;
//        return jpaQueryFactory
//                .select(qHotel.id)
//                .from(qHotel)
//                .orderBy(qHotel.id.asc())
//                .limit(BATCH)
//                .offset(offset)
//                .fetch();
//    }
//    public Flux<List<Long>> idBatches() {
//        return Flux.<List<Long>, Integer>generate(
//                () -> 0, // state: page
//                (page, sink) -> {
//                    List<Long> ids = fetchIdsDSL(page); // 블로킹
//                    if (ids.isEmpty()) {
//                        sink.complete();
//                    } else {
//                        sink.next(ids);
//                    }
//                    log.info("idBatches: {}", ids);
//                    return page + 1;
//                }
//        ).subscribeOn(Schedulers.boundedElastic()); // 블로킹을 별도 스케줄러로
//    }
//
//    /** 호텔 하나 처리: 외부 API → 트랜잭션 저장(attach 후 더티체킹) */
//    private Mono<Void> processOne(Long hotelId) {
//        log.info(String.valueOf(hotelId));
//        return agodaIdScrapper(hotelId)
//                .timeout(Duration.ofSeconds(8))
//                .retryWhen(Retry.backoff(3, Duration.ofMillis(300)).filter(this::isRetriable))
//                .onErrorResume(e -> Mono.empty()) // API 실패는 스킵
//                .flatMap(review ->
//                        Mono.fromRunnable(() ->
//                                tx.executeWithoutResult(st -> {
//                                    Hotel hotel = hotelRepo.findById(hotelId).orElseThrow(); // attach
//                                    var demo = review.getScore().getDemographics();
//                                    var agoda   = demo.stream().filter(d -> d.getProviderId()==332).toList();
//                                    var booking = demo.stream().filter(d -> d.getProviderId()==3038).toList();
//
//                                    List<HotelReview> toSave = new ArrayList<>(2);
//                                    toSave.add(reviewSeparator(agoda,   hotel, ReviewBrand.AGODA));
//                                    toSave.add(reviewSeparator(booking, hotel, ReviewBrand.BOOKINGDOTCOM));
//
//                                    hotelReviewRepo.saveAll(toSave);
//                                    hotel.getHotelReviewList().addAll(toSave); // 연관 더티체킹
//                                    averageScoreCalculator(hotel);             // 필드 더티체킹
//                                })
//                        ).subscribeOn(Schedulers.boundedElastic())
//                ).then();
//    }
//
//    public Mono<AgodaReviewResponse> agodaIdScrapper(long id) {
//
//        return agodaWebClient.post()
//                .uri("/cronos/property/review/HotelReviews")
//                .accept(MediaType.APPLICATION_JSON)
//                .bodyValue(new AgodaReviewRequest(id))
//                .retrieve()
//                .bodyToMono(AgodaReviewResponse.class);
//    }
//
//    private boolean isRetriable(Throwable t) {
//        if (t instanceof TimeoutException) return true; // .timeout(...)과 세트
//        if (t instanceof ConnectException || t instanceof UnknownHostException) return true;
//        if (t instanceof PrematureCloseException) return true; // 서버가 커넥션 조기 종료
//        if (t instanceof WebClientResponseException wcre) {
//            int s = wcre.getStatusCode().value();
//            return s == 429 || (s >= 500 && s < 600); // 5xx/429만 재시도
//        }
//        return false; // 나머지는 즉시 실패
//    }
//
//    private void averageScoreCalculator(Hotel hotel) {
//
//        double averageAllScore = 0;int allCnt = 0;
//        double averageBusinessScore = 0;int businessCnt = 0;
//        double averageCoupleScore = 0;int coupleCnt = 0;
//        double averageSoloScore = 0;int soloCnt = 0;
//        double averageFamilyScore = 0;int familyCnt = 0;
//        double averageGroupScore = 0;int groupCnt = 0;
//
//        for (HotelReview hr : hotel.getHotelReviewList()) {
//            if (hr.getAllScore() != 0)      { averageAllScore      += hr.getAllScore();      allCnt++; }
//            if (hr.getBusinessScore() != 0) { averageBusinessScore += hr.getBusinessScore(); businessCnt++; }
//            if (hr.getCoupleScore() != 0)   { averageCoupleScore   += hr.getCoupleScore();   coupleCnt++; }
//            if (hr.getSoloScore() != 0)     { averageSoloScore     += hr.getSoloScore();     soloCnt++; }
//            if (hr.getFamilyScore() != 0)   { averageFamilyScore   += hr.getFamilyScore();   familyCnt++; }
//            if (hr.getGroupScore() != 0)    { averageGroupScore    += hr.getGroupScore();    groupCnt++; }
//        }
//
//        hotel.averageScoreUpdate(
//                avgOrZero(averageAllScore,      allCnt),
//                avgOrZero(averageBusinessScore, businessCnt),
//                avgOrZero(averageCoupleScore,   coupleCnt),
//                avgOrZero(averageSoloScore,     soloCnt),
//                avgOrZero(averageFamilyScore,   familyCnt),
//                avgOrZero(averageGroupScore,    groupCnt)
//        );
//
//    }
//    private static double avgOrZero(double sum, int cnt) {
//        if (cnt <= 0) return 0.0;                    // 정책상 0.0로 저장 (원하면 다른 기본값)
//        double v = sum / cnt;
//        return Double.isFinite(v) ? v : 0.0;         // Infinity/NaN 방지
//    }
//    private HotelReview reviewSeparator(List<AgodaReviewResponse.Demographic> dList, Hotel h, ReviewBrand reviewBrand) {
//        HotelReview hr = new HotelReview();
//
//        for (AgodaReviewResponse.Demographic d : dList) {
//            switch (d.getName()) {
//                case "All guests": {
//                    hr.setReviewCount(d.getCount());
//                    hr.setAllScore(d.getScore());
//                    break;
//                }
//                case "Business travelers": {
//                    hr.setBusinessScore(d.getScore());
//                    break;
//                }
//                case "Couples" : {
//                    hr.setCoupleScore(d.getScore());
//                    break;
//                }
//                case "Solo travelers" : {
//                    hr.setSoloScore(d.getScore());
//                    break;
//                }
//                case "Families with young children": case "Families with teens" : {
//                    hr.setFamilyScore(d.getScore());
//                    break;
//                }
//                case "Groups": {
//                    hr.setGroupScore(d.getScore());
//                    break;
//                }
//            }
//        }
//        hr.setReviewBrand(reviewBrand);
//        hr.setHotel(h);
//
//        return hr;
//    }
//}
