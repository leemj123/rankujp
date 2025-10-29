package com.lee.rankujp.big;

import com.lee.rankujp.hotel.infra.Hotel;
import com.lee.rankujp.hotel.infra.HotelCity;
import com.lee.rankujp.hotel.infra.QHotelCity;
import com.lee.rankujp.hotel.repo.HotelRepo;
import com.lee.rankujp.hotel.review.HotelReviewService;
import com.lee.rankujp.hotel.review.scrapper.JalanService;
import com.lee.rankujp.hotel.review.scrapper.TripService;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BigService {

    private final JPAQueryFactory jpaQueryFactory;
    private final QAgodaKo qAgodaKo = QAgodaKo.agodaKo;
    private final QAgodaCity qAgodaCity = QAgodaCity.agodaCity;
    private final QHotelCity qHotelCity = QHotelCity.hotelCity;
    private final HotelRepo hotelRepo;

    private final HotelReviewService hotelReviewService;
    private final TripService tripService;
    private final JalanService jalanService;


//    @Transactional
//    public void citySeparate() {
//        List<AgodaCity> entityList = jpaQueryFactory
//                .selectFrom(qAgodaCity)
//                .fetch();
//
//        for (AgodaCity e : entityList) {
//            Long count = jpaQueryFactory
//                    .select(qAgodaKo.hotel_id.count())
//                    .from(qAgodaKo)
//                    .where(qAgodaKo.city_id.eq(e.getCityId()))
//                    .fetchOne();
//
//            e.setCount(count != null ? count : 0L);
//
//        }
//    }
//
//    public void firstQueue() {
//        List<HotelCity> cityList = jpaQueryFactory
//                .selectFrom(qHotelCity)
//                .where( qHotelCity.id.in(
//                        144,
//                        193,
//                        1568,
//                        6263,
//                        8563,
//                        13561,
//                        16527,
//                        106058,
//                        106491,
//                        107729,
//                        107890,
//                        108162,
//                        108182,
//                        255582
//                ))
//                .fetch();
//        List<Hotel> hotelList = new ArrayList<>();
//
//        for (HotelCity e : cityList) {
//
//            List<AgodaKo> eList = jpaQueryFactory
//                    .selectFrom(qAgodaKo)
//                    .where(qAgodaKo.city_id.eq(e.getId()))
//                    .fetch();
//
//            hotelList.addAll(eList.stream().map(p -> new Hotel(p, e)).toList());
//        }
//
//        hotelRepo.saveAll(hotelList);
//    }

    public Mono<Void> secondQueue() {
        //아고다
        Mono<Void> run = hotelReviewService.syncAllReviews()
                .doOnSubscribe(s -> log.info("[/agoda] sync started"))
                .doOnError(e -> log.error("[/agoda] sync failed", e))
                .doOnSuccess(v -> log.info("[/agoda] sync completed"))
                .doFinally(sig -> log.info("[/agoda] finally: {}", sig));

        run.subscribe();

        tripService.startReviewScrap();
//        jalanService.startReviewScrap();

        return Mono.just("fin").then();
    }
}
