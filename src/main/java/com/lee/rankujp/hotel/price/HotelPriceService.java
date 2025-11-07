package com.lee.rankujp.hotel.price;

import com.lee.rankujp.hotel.infra.Hotel;
import com.lee.rankujp.hotel.infra.HotelPrice;
import com.lee.rankujp.hotel.infra.QHotel;
import com.lee.rankujp.hotel.infra.QHotelPrice;
import com.lee.rankujp.hotel.price.dto.AgodaPriceRequest;
import com.lee.rankujp.hotel.price.dto.AgodaPriceResponse;
import com.lee.rankujp.hotel.price.dto.HotelPriceRow;
import com.lee.rankujp.hotel.price.function.PriceNormalize;
import com.lee.rankujp.hotel.review.RankuScoreCalculator;
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
    private final QHotelPrice qHotelPrice = QHotelPrice.hotelPrice;

    private final WebClient agodaApiClient;
    private final HotelPersistService persistService;
    private static final Duration BASE = Duration.ofMillis(10);
    private static final long JITTER_MS = 10;

    private static Duration jittered() {
        long j = ThreadLocalRandom.current().nextLong(-JITTER_MS, JITTER_MS + 1);
        return BASE.plusMillis(j);
    }

    // 배치/DB 전용 boundedElastic: 코어 16, 최대 32, 큐 2,000 (예시)
    static final Scheduler DB_ELASTIC =
            Schedulers.newBoundedElastic(
                    16,            // 최대 동시 스레드 수
                    2000,          // 큐 용량 제한
                    "db-elastic",
                    60,            // idle 60초 유지 후 스레드 제거
                    true            // daemon
            );

    // 파이프라인 동시성은 커넥션풀/쿼리특성 고려해 보수적으로
    private static final int BATCH_CONCURRENCY = 8; // (예시) Hikari batch 풀 20이라면 8~12 권장
    private static final int BATCH = 100;

    public Flux<List<Long>> idBatches() {
        return Flux.<List<Long>, Integer>generate(
                () -> 0, // state: page
                (page, sink) -> {

                    List<Long> ids = this.fetchIdsDSL(page); // 블로킹

                    if (ids.isEmpty())
                        sink.complete();
                     else
                        sink.next(ids);

                    return page + 1;
                }
        ).publishOn(DB_ELASTIC, 1);
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
        return idBatches()
                    .flatMap(ids ->
                                    priceNormalize(baseDate, ids)
                                            .flatMap(topMap ->
                                                    persistService.reloadHotelPrices(topMap)
                                                            .subscribeOn(DB_ELASTIC)
                                                            .retryWhen(
                                                                    reactor.util.retry.Retry.backoff(3, Duration.ofMillis(2))
                                                            )
                                            )
                            , BATCH_CONCURRENCY, 1)
                .then();
    }

    public Mono<List<HotelPriceRow>> priceNormalize(LocalDate baseDate, List<Long> ids) {
        return priceProcessFluxWithDate(baseDate, ids).collectList();
    }

    public Flux<HotelPriceRow> priceProcessFluxWithDate(LocalDate stayDate, List<Long> ids) {
        return Flux.range(0, 45)
                .map(stayDate::plusDays)
                .concatMap(day ->
                                Mono.delay(jittered())
                                        .then(callApiForDay(day, day.plusDays(2), ids))
                                        .map(resp -> PriceNormalize.normalizeHotelPrice(ids, resp, day)) // 정규화
                                        .flatMapMany(Flux::fromIterable)
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

    //========================================================================
    @Transactional
    public void scoreCalculator(){
        List<Hotel> hotels = jpaQueryFactory
                .selectFrom(qHotel)
                .fetch();

        hotels.stream().forEach(hotel -> hotel.rankuScoreUpdater(RankuScoreCalculator.hotelScore(hotel)) );
    }

    public Mono<Void> bestPriceChoicerBatch() {
        return idBatches() // Flux<List<Long>>
                .flatMap(batchIds ->
                                Flux.fromIterable(batchIds)
                                        .flatMap(hotelId ->
                                                        bestPriceChooserReactive(hotelId) // Mono<HotelPrice>
                                                                .onErrorResume(e -> {
                                                                    log.warn("bestPriceChooserReactive 실패 hotelId={}", hotelId, e);
                                                                    return Mono.empty();
                                                                }),
                                                BATCH_CONCURRENCY
                                        )
                                        .collectList() // List<HotelPrice>
                                        .filter(list -> !list.isEmpty())
                                        .flatMap(list ->
                                                persistService.reviseHotelBestPrice(list) // Mono<Void>
                                                        .subscribeOn(DB_ELASTIC)
                                                        .retryWhen(
                                                                reactor.util.retry.Retry.backoff(3, Duration.ofMillis(200))
                                                        )
                                        ),
                        BATCH_CONCURRENCY
                )
                .then();
    }

    private HotelPrice pickBest(List<HotelPrice> prices) {
        if (prices == null || prices.isEmpty()) return null;

        // 동률일 때: 일일요금이 낮은 것이 "더 좋은 가격"으로 간주하려면 반대로 정렬
        // 낮은 dailyRate 우선
        return prices.stream()
                .filter(Objects::nonNull)
                .filter(p -> !Double.isNaN(p.getSalePercent()))
                .filter(p -> p.getSalePercent() >= 0)
                .max(
                        Comparator.comparingDouble(HotelPrice::getSalePercent)
                                .thenComparingDouble(HotelPrice::getDailyRate)
                )
                .orElse(null);
    }

    private Mono<HotelPrice> bestPriceChooserReactive(long hotelId) {
        return loadPriceByIdReactive(hotelId) // Mono<List<HotelPrice>>
                .map(this::pickBest)              // HotelPrice 또는 null
                .flatMap(best -> best != null ? Mono.just(best) : Mono.empty());
    }
    private Mono<List<HotelPrice>> loadPriceByIdReactive(long hotelId) {
        return Mono.fromCallable(() ->
                        jpaQueryFactory
                                .selectFrom(qHotelPrice)
                                .where(qHotelPrice.id.hotelId.eq(hotelId))
                                .fetch()
                )
                .subscribeOn(DB_ELASTIC);
    }


}
