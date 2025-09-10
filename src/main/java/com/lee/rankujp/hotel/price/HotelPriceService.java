package com.lee.rankujp.hotel.price;

import com.lee.rankujp.hotel.cumtom.HotelPriceRow;
import com.lee.rankujp.hotel.infra.QHotel;
import com.lee.rankujp.hotel.infra.QHotelPrice;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLInsertClause;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelPriceService {

    private final JPAQueryFactory jpaQueryFactory;
    private final SQLQueryFactory sqlQueryFactory;

    private final QHotel qHotel = QHotel.hotel;
    private final QHotelPrice qHotelPrice = QHotelPrice.hotelPrice;
    private final WebClient agodaApiClient;

    private static final int CONCURRENCY = 8;
    private static final int BATCH = 100;
    private static final Duration BASE = Duration.ofMillis(100);
    private static final long JITTER_MS = 200;

    public List<AgodaPriceResponse.HotelApiInfo> getAgodaPrice() {
        return null;
//        List<Long> hotelIds = jpaQueryFactory
//                .select(qHotel.id)
//                .from(qHotel)
//                .orderBy(qHotel.id.asc())
//                .limit(100)
//                .fetch();
//
//        AgodaPriceResponse agodaPriceResponse =  this.agodaPriceResponseMono(LocalDate.now().plusDays(7),LocalDate.now().plusDays(9), hotelIds).block();
//        return agodaPriceResponse.getResults().stream()
//                .sorted(Comparator.comparing( AgodaPriceResponse.HotelApiInfo::getHotelId ))
//                .collect(Collectors.toList());
    }



//    private static Duration jittered() {
//        long j = ThreadLocalRandom.current().nextLong(-JITTER_MS, JITTER_MS + 1);
//        return BASE.plusMillis(j);
//    }
//    public Mono<Void> syncAllReviews() {
//        return idBatches()
//                .concatMap(ids -> {
//                    return Flux.fromIterable(ids)
//                            .concatMap(id -> Mono.delay(jittered()).thenReturn(id))
//                            .flatMap(this::agodaPriceResponseMono, CONCURRENCY);
//                },1)
//                .then();
//    }
//
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
//
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
//    public Mono<Void> agodaPriceResponseMono(LocalDate checkInDate, LocalDate checkOutDate, List<Long> hotelId) {
//        AgodaPriceRequest agodaPriceRequest = new AgodaPriceRequest(checkInDate, checkOutDate, hotelId);
//
//        return agodaApiClient.post()
//                .uri("/affiliateservice/lt_v1")
//                .accept(MediaType.APPLICATION_JSON)
//                .bodyValue(agodaPriceRequest)
//                .retrieve()
//                .onStatus(HttpStatusCode::isError, resp ->
//                        resp.bodyToMono(String.class)
//                                .flatMap(body -> Mono.error(new IllegalStateException(
//                                        "Agoda API error %s: %s".formatted(resp.statusCode(), body)))))
//                .bodyToMono(AgodaPriceResponse.class)
//                .timeout(Duration.ofSeconds(20)); // 요청 타임아웃
//    }

    //QueryDSL SQL (Upsert)

//    public long upsertBatch(List<HotelPriceRow> rows) {
//        SQLInsertClause ins = sqlQueryFactory.insert(qHotelPrice)
//                .onDuplicateKeyUpdate()  // 공통 UPSERT 정책
//                .set(qHotelPrice.crossedOutRate, (Double) null) // 자리잡기용, 개별 set은 addBatch 때
//                .set(qHotelPrice.dailyRate,      (Double) null)
//                .set(qHotelPrice.sailPercent,    (Double) null);
//
//        for (HotelPriceRow r : rows) {
//            ins
//                    .set(qHotelPrice.id, r.hotelId())
//                    .set(qHotelPrice.stayDate, r.stayDate())
//                    .set(qHotelPrice.crossedOutRate, r.crossedOutRate())
//                    .set(qHotelPrice.dailyRate, r.dailyRate())
//                    .set(qHotelPrice.sailPercent, r.sailPercent())
//                    .addBatch();
//        }
//        return ins.execute(); // 한 번에 INSERT ... ON DUPLICATE KEY UPDATE
//    }

}
