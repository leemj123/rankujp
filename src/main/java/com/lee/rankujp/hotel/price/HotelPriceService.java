package com.lee.rankujp.hotel.price;

import com.lee.rankujp.hotel.infra.Hotel;
import com.lee.rankujp.hotel.infra.QHotel;
import com.lee.rankujp.hotel.price.dto.AgodaPriceRequest;
import com.lee.rankujp.hotel.price.dto.AgodaPriceResponse;
import com.lee.rankujp.hotel.price.dto.ImgStarResponse;
import com.lee.rankujp.hotel.price.dto.TopBucket;
import com.lee.rankujp.hotel.price.function.PriceNormalize;
import com.lee.rankujp.hotel.price.function.TopKCollectors;
import com.lee.rankujp.hotel.repo.HotelRepo;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelPriceService {

    private final JPAQueryFactory jpaQueryFactory;

    private final QHotel qHotel = QHotel.hotel;
    private final WebClient agodaApiClient;
    private final HotelPersistService persistService;
    private final HotelRepo hotelRepo;

    private static final int BATCH_CONCURRENCY = 8;
    private static final int BATCH = 100;
    private static final Duration BASE = Duration.ofMillis(10);
    private static final long JITTER_MS = 10;


    private static Duration jittered() {
        long j = ThreadLocalRandom.current().nextLong(-JITTER_MS, JITTER_MS + 1);
        return BASE.plusMillis(j);
    }

    static final Scheduler DB_ELASTIC =
            Schedulers.newBoundedElastic(10, Integer.MAX_VALUE, "db-elastic");

    public Flux<List<Long>> idBatches() {
        return Flux.<List<Long>, Integer>generate(
                () -> 0, // state: page
                (page, sink) -> {

                    List<Long> ids = this.fetchIdsDSL(page); // 블로킹

                    if (ids.isEmpty())
                        sink.complete();
                     else
                        sink.next(ids);

                    log.info("idBatches: {}", ids);
                    return page + 1;
                }
        ).subscribeOn(Schedulers.boundedElastic()); // 블로킹을 별도 스케줄러로
    }

    public List<Long> fetchIdsDSL (int page) {
        long offset = (long) page * BATCH;
        return jpaQueryFactory
                .select(qHotel.id)
                .from(qHotel)
                .orderBy(qHotel.id.asc())
                .limit(BATCH)
                .offset(offset)
                .fetch();
    }

    public Mono<Void> syncAllPriceWindowBatched() {
        LocalDate baseDate = LocalDate.now();
        return persistService.truncateHotelPrice()
                .thenMany(
                        // Flux<List<Long>> (각 100개라고 가정)
                        idBatches()
                                .flatMap(ids ->
                                                buildTop10(baseDate, ids)
                                                        .flatMap(topMap ->
                                                                persistService.reloadHotelPrices(topMap)
                                                                        .subscribeOn(DB_ELASTIC)
                                                                        .retryWhen(
                                                                                reactor.util.retry.Retry.backoff(3, Duration.ofMillis(200))
                                                                        )
                                                        )
                                        , BATCH_CONCURRENCY, 1)
                )
                .then();
    }

    public Mono<Map<Long, TopBucket>> buildTop10(LocalDate baseDate, List<Long> ids) {
        final Set<Long> idSet = new HashSet<>(ids);

        return priceProcessFluxWithDate(baseDate, ids) // Flux<Tuple2<LocalDate, AgodaPriceResponse>>
                .flatMap(t -> {
                    LocalDate day = t.getT1();
                    AgodaPriceResponse resp = t.getT2();
                    if (resp.getResults() == null) return Flux.empty();

                    return Flux.fromIterable(resp.getResults())
                            .filter(info -> idSet.contains(info.getHotelId()))
                            .map(info -> Tuples.of(info.getHotelId(), PriceNormalize.normalize(day, info)));
                })
                .collect(TopKCollectors.top5WeekdayWeekendPerHotel());
    }

    public Flux<Tuple2<LocalDate, AgodaPriceResponse>> priceProcessFluxWithDate(LocalDate stayDate, List<Long> ids) {
        return Flux.range(0, 45)
                .map(stayDate::plusDays)
                .concatMap(day ->
                                Mono.delay(jittered())
                                        .then(callApiForDay(day, day.plusDays(2), ids)
                                                .map(resp -> Tuples.of(day, resp)))
                        , 1);
    }


    public Mono<AgodaPriceResponse> callApiForDay(LocalDate stayDate, LocalDate finDate, List<Long> hotelId) {

        return agodaApiClient.post()
                .uri("/affiliateservice/lt_v1")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new AgodaPriceRequest(stayDate, finDate, hotelId))
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new IllegalStateException(
                                        "Agoda API error %s".formatted(resp.statusCode())))))
                .bodyToMono(AgodaPriceResponse.class);
    }

//============================================================

    private static final int MAX_DAY_SHIFT = 45;          // 최대 45번(=45일)까지 날짜 이동 시도
    private static final Duration REQ_TIMEOUT = Duration.ofSeconds(15);

    @Transactional
    public void imgAndStarUpdate() {
        List<Hotel> hotels = hotelRepo.findAll();
        LocalDate baseStay = LocalDate.now().plusDays(20);

        for (Hotel h : hotels) {
            LocalDate stayDate = baseStay;
            LocalDate finDate  = stayDate.plusDays(2);

            boolean updated = false;

            for (int shift = 0; shift < MAX_DAY_SHIFT; shift++) {
                try {
                    ImgStarResponse resp = requestAgodaBlocking(stayDate, finDate, h.getId());

                    if (resp != null && resp.getError() == null) {
                        h.imgStarUpdate(resp.getResults().get(0));
                        updated = true;
                        break;
                    } else {
                        // 에러 응답 → 날짜 +1일 이동 후 재시도
                        log.info("Agoda error for hotel {} on {}~{} → shift+1. reason={}",
                                h.getId(), stayDate, finDate,
                                resp != null && resp.getError() != null ? resp.getError().getMessage() : "unknown");
                    }
                } catch (Exception e) {
                    log.warn("Request failed for hotel {} on {}~{} → shift+1. err={}",
                            h.getId(), stayDate, finDate, e.toString());
                }

                // 날짜 +1일씩 이동
                stayDate = stayDate.plusDays(1);
                finDate  = finDate.plusDays(1);

            }

            if (!updated) {
                log.warn("No valid response within {} shifts for hotel {}", MAX_DAY_SHIFT, h.getId());
            } else {
                log.info("{} updated", h.getId());
            }
        }
    }

    private ImgStarResponse requestAgodaBlocking(LocalDate stayDate, LocalDate finDate, long hotelId) {
        return agodaApiClient.post()
                .uri("/affiliateservice/lt_v1")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new AgodaPriceRequest(stayDate, finDate, hotelId))
                .retrieve()
                .onStatus(HttpStatusCode::isError, r ->
                        r.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new IllegalStateException(
                                        "Agoda API error %s: %s".formatted(r.statusCode(), body)))))
                .bodyToMono(ImgStarResponse.class)
                .timeout(REQ_TIMEOUT)
                .block();
    }

}
