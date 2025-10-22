package com.lee.rankujp.hotel.review;

import com.lee.rankujp.hotel.cumtom.ReviewBrand;
import com.lee.rankujp.hotel.infra.Hotel;
import com.lee.rankujp.hotel.infra.HotelReview;
import com.lee.rankujp.hotel.infra.QHotel;
import com.lee.rankujp.hotel.infra.QHotelReview;
import com.lee.rankujp.hotel.repo.HotelRepo;
import com.lee.rankujp.hotel.repo.HotelReviewRepo;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.PrematureCloseException;
import reactor.util.retry.Retry;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelReviewService {

    private final WebClient agodaWebClient;
    private final HotelRepo hotelRepo;
    private final HotelReviewRepo hotelReviewRepo;
    private final TransactionTemplate tx;

    private static final int CONCURRENCY = 8;
    private static final int BATCH = 1000;
    private static final Duration BASE = Duration.ofMillis(500);

    private final JPAQueryFactory jpaQueryFactory;
    private final QHotel qHotel = QHotel.hotel;
    private final QHotelReview qHotelReview = QHotelReview.hotelReview;


    public Mono<Void> syncAllReviews() {
        return idBatches()
                .concatMap(ids -> {
                    return Flux.fromIterable(ids)
                            .concatMap(id -> Mono.delay(BASE).thenReturn(id))
                            .flatMap(this::processOne, CONCURRENCY);
                },1)
                .then();
    }

    public List<Long> fetchIdsDSL (int page) {
        long offset = (long) page * BATCH;
        return jpaQueryFactory
                .select(qHotel.id)
                .from(qHotel)
                .leftJoin(qHotel.hotelReviewList, qHotelReview)
                .on(qHotelReview.reviewBrand.eq(ReviewBrand.AGODA))
                .where(qHotelReview.id.isNull())
                .orderBy(qHotel.id.asc())
                .limit(BATCH)
                .offset(offset)
                .fetch();
    }

    public Flux<List<Long>> idBatches() {
        return Flux.<List<Long>, Integer>generate(
                () -> 0, // state: page
                (page, sink) -> {
                    List<Long> ids = fetchIdsDSL(page); // 블로킹
                    if (ids.isEmpty()) {
                        sink.complete();
                    } else {
                        sink.next(ids);
                    }
                    log.info("batch cnt: {}", ids.size());
                    return page + 1;
                }
        ).subscribeOn(Schedulers.boundedElastic()); // 블로킹을 별도 스케줄러로
    }

    /** 호텔 하나 처리: 외부 API → 트랜잭션 저장(attach 후 더티체킹) */
    private Mono<Void> processOne(Long hotelId) {
        log.info(String.valueOf(hotelId));
        return agodaIdScrapper(hotelId)
                .timeout(Duration.ofSeconds(8))
                .retryWhen(Retry.backoff(3, Duration.ofMillis(300)).filter(this::isRetriable))
                .onErrorResume(e -> Mono.empty()) // API 실패는 스킵
                .flatMap(review ->
                        Mono.fromRunnable(() ->
                                tx.executeWithoutResult(st -> {
                                    Hotel hotel = hotelRepo.findById(hotelId).orElseThrow(); // attach
                                    var demo = review.getScore().getDemographics();
                                    var agoda = demo.stream().filter(d -> d.getProviderId()==332).toList();

                                    HotelReview hotelReview = reviewSeparator(agoda, hotel);
                                    hotelReviewRepo.save(hotelReview);
                                })
                        ).subscribeOn(Schedulers.boundedElastic())
                ).then();
    }

    public Mono<AgodaReviewResponse> agodaIdScrapper(long id) {

        return agodaWebClient.post()
                .uri("/cronos/property/review/HotelReviews")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new AgodaReviewRequest(id))
                .retrieve()
                .bodyToMono(AgodaReviewResponse.class);
    }

    private boolean isRetriable(Throwable t) {
        if (t instanceof TimeoutException) return true; // .timeout(...)과 세트
        if (t instanceof ConnectException || t instanceof UnknownHostException) return true;
        if (t instanceof PrematureCloseException) return true; // 서버가 커넥션 조기 종료
        if (t instanceof WebClientResponseException wcre) {
            int s = wcre.getStatusCode().value();
            return s == 429 || (s >= 500 && s < 600); // 5xx/429만 재시도
        }
        return false; // 나머지는 즉시 실패
    }

//    private void averageScoreCalculator(Hotel hotel, List<AgodaReviewResponse.Demographic> agoda) {
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
//    }


//    private static double avgOrZero(double sum, int cnt) {
//        if (cnt <= 0) return 0.0;                    // 정책상 0.0로 저장 (원하면 다른 기본값)
//        double v = sum / cnt;
//        return Double.isFinite(v) ? v : 0.0;         // Infinity/NaN 방지
//    }

    private HotelReview reviewSeparator(List<AgodaReviewResponse.Demographic> dList, Hotel h) {
        HotelReview hr = new HotelReview();
        double v = 0;
        double v1 = 0;
        double v2 = 0;
        double v3 = 0;
        double v4 = 0;
        double v5 = 0;
        long count = 0;

        for (AgodaReviewResponse.Demographic d : dList) {
            count += d.getCount();

            switch (d.getName()) {
                case "All guests": {
                    hr.setAllScore(d.getScore());
                    v += d.getScore();
                    break;
                }
                case "Business travelers": {
                    v1 += d.getScore();
                    break;
                }
                case "Couples" : {
                    v2 += d.getScore();
                    break;
                }
                case "Solo travelers" : {
                    v3 += d.getScore();
                    break;
                }
                case "Families with young children": case "Families with teens" : {
                    if (v4 != 0) {
                        v4 += d.getScore();
                        double temp = (v4 + d.getScore()) / 2;
                        v4 = Math.floor(temp * 10) / 10.0;
                    } else {
                        v4 += d.getScore();
                    }

                    break;
                }
                case "Groups": {
                    v5 += d.getScore();
                    break;
                }
            }
        }
        h.averageScoreUpdate(count, v, v1, v2, v3, v4, v5);
        hr.setReviewBrand(ReviewBrand.AGODA);
        hr.setReviewCount(count);

        hr.setHotel(h);

        return hr;
    }

    //tkrwp
    @Transactional
    public void hotelFReviewRevise() {
        List<Hotel> hlist = jpaQueryFactory
                .selectFrom(qHotel)
                .where(qHotel.averageFamilyScore.gt(10))
                .fetch();
        log.info("lenth: {}", hlist.size());

        for (Hotel h : hlist) {
            h.faUp();
        }
    }
}
