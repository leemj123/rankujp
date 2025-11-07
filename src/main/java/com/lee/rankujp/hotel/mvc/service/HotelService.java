package com.lee.rankujp.hotel.mvc.service;

import com.lee.rankujp.hotel.cumtom.PointLocation;
import com.lee.rankujp.hotel.cumtom.ReviewBrand;
import com.lee.rankujp.hotel.infra.*;
import com.lee.rankujp.hotel.mvc.dto.*;
import com.lee.rankujp.hotel.price.HotelPriceService;
import com.lee.rankujp.hotel.price.dto.AgodaPriceResponse;
import com.lee.rankujp.hotel.repo.HotelRepo;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelService {
    private final JPAQueryFactory jpaQueryFactory;
    private final HotelRepo hotelRepo;
    private final QHotel qHotel = QHotel.hotel;
    private final QHotelPrice qHotelPrice = QHotelPrice.hotelPrice;
    private final QHotelCity qHotelCity = QHotelCity.hotelCity;
    private final QHotelReview qHotelReview = QHotelReview.hotelReview;
    private final HotelPriceService hotelPriceService;

    //list==================================

    public Page<HotelWithPrice> salePage(int location, int sort, int page, LocalDate searchDate, boolean price) {
        Pageable pageable = PageRequest.of(page-1, 20);
        //
        BooleanExpression predicate = qHotel.prefectureCode.eq(1).and(qHotel.isShow.isTrue());

        //정렬 null들어가면 에러나서 커버
        List<OrderSpecifier<?>> orders = new ArrayList<>();
        OrderSpecifier<?> order = this.orderType(sort);

        //온천판단
        if (sort == 6) {
            predicate = predicate.and(qHotel.isOnsen.isTrue());
        }

        if (searchDate == null) {
            predicate = predicate.and(qHotel.bestDailyRate.ne(0.0));
            predicate = this.kansaiFilterQueryExpression(predicate, location);

            if (order != null) {
                orders.add(order);
                predicate = predicate.and(qHotel.reviewNum.gt(300).and(qHotel.bestSalePrecent.ne(0.0)));
            }
            if (price) {
                orders.add(qHotel.bestDailyRate.asc());
                orders.add(qHotel.bestSalePrecent.desc());
            } else {
                orders.add(qHotel.bestSalePrecent.desc());
                orders.add(qHotel.rankuScore.desc());
            }
        } else {
            predicate = predicate.and(qHotelPrice.id.stayDate.eq(searchDate).and(qHotelPrice.dailyRate.ne(0.0)));
            predicate = this.kansaiFilterQueryExpression(predicate, location);

            if (price) {
                orders.add(qHotelPrice.dailyRate.asc());
                orders.add(qHotelPrice.salePercent.desc());
                if (order != null) {
                    orders.add(order);
                    predicate = predicate.and(qHotel.reviewNum.gt(300).and(qHotelPrice.salePercent.ne(0.0)));
                }

            } else {
                if (order != null) {
                    orders.add(order);
                    predicate = predicate.and(qHotel.reviewNum.gt(300).and(qHotelPrice.salePercent.ne(0.0)));
                }
                orders.add(qHotelPrice.salePercent.desc());
                orders.add(qHotel.rankuScore.desc());
            }
        }


        if (searchDate != null) {

            List<HotelWithPrice> results = jpaQueryFactory
                    .select(
                        Projections.constructor(HotelWithPrice.class,
                                qHotel.id,
                                qHotel.thumbnailImg,
                                qHotel.koName,
                                qHotel.starRating,
                                qHotelPrice.crossedOutRate,
                                qHotelPrice.dailyRate,
                                qHotelPrice.salePercent,
                                qHotel.averageBusinessScore,
                                qHotel.averageCoupleScore,
                                qHotel.averageSoloScore,
                                qHotel.averageFamilyScore,
                                qHotel.isOnsen
                    ))
                    .from(qHotelPrice)
                    .join(qHotel).on(qHotel.id.eq(qHotelPrice.id.hotelId))
                    .where(predicate)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .orderBy(orders.toArray(new OrderSpecifier[0]))
                    .fetch();

            long total = Optional.ofNullable(
                    jpaQueryFactory
                            .select(qHotelPrice.id.count())
                            .from(qHotelPrice)
                            .join(qHotel).on(qHotel.id.eq(qHotelPrice.id.hotelId))
                            .where(predicate)
                            .fetchOne()
            ).orElse(0L);

            return new PageImpl<>(results, pageable, total);
        }
        else {
            List<HotelWithPrice> results = jpaQueryFactory
                    .select(
                            Projections.constructor(HotelWithPrice.class,
                                    qHotel.id,
                                    qHotel.thumbnailImg,
                                    qHotel.koName,
                                    qHotel.starRating,
                                    qHotel.bestCrossedOutRate,
                                    qHotel.bestDailyRate,
                                    qHotel.bestSalePrecent,
                                    qHotel.averageBusinessScore,
                                    qHotel.averageCoupleScore,
                                    qHotel.averageSoloScore,
                                    qHotel.averageFamilyScore,
                                    qHotel.isOnsen
                            ))
                    .from(qHotel)
                    .where(predicate)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .orderBy(orders.toArray(new OrderSpecifier[0]))
                    .fetch();

            long total = Optional.ofNullable(
                    jpaQueryFactory
                            .select(qHotel.id.count())
                            .from(qHotel)
                            .where(predicate)
                            .fetchOne()
            ).orElse(0L);

            return new PageImpl<>(results, pageable, total);
        }


    }

    public Page<HotelWithScore> scorePage(int location, int sort, int page, LocalDate searchDate) {
        Pageable pageable = PageRequest.of(page-1, 20);
        //
        BooleanExpression predicate = qHotel.prefectureCode.eq(1).and(qHotel.isShow.isTrue());

        //온천판단
        if (sort == 6) {
            predicate = predicate.and(qHotel.isOnsen.isTrue());
        }

        if (searchDate == null) {
            predicate = predicate.and(qHotel.bestDailyRate.ne(0.0));
            predicate = this.kansaiFilterQueryExpression(predicate, location);
        } else {
            predicate = predicate.and(qHotelPrice.id.stayDate.eq(searchDate).and(qHotelPrice.dailyRate.ne(0.0)));
            predicate = this.kansaiFilterQueryExpression(predicate, location);
        }

        //정렬 null들어가면 에러나서 커버
        List<OrderSpecifier<?>> orders = new ArrayList<>();
        OrderSpecifier<?> order = this.orderType(sort);

        if (order != null) {
            orders.add(order);
            predicate = predicate.and(qHotel.reviewNum.gt(300).and(qHotel.bestSalePrecent.ne(0.0)));
        }
        orders.add(qHotel.rankuScore.desc());
        orders.add(qHotel.starRating.desc());

        if (searchDate != null) {

            List<HotelWithScore> results = jpaQueryFactory
                    .select(
                            Projections.constructor(HotelWithScore.class,
                                    qHotel.id,
                                    qHotel.thumbnailImg,
                                    qHotel.koName,
                                    qHotel.starRating,
                                    qHotel.rankuScore,
                                    qHotel.averageBusinessScore,
                                    qHotel.averageCoupleScore,
                                    qHotel.averageSoloScore,
                                    qHotel.averageFamilyScore,
                                    qHotel.isOnsen
                            ))
                    .from(qHotelPrice)
                    .join(qHotel).on(qHotel.id.eq(qHotelPrice.id.hotelId))
                    .join(qHotel.hotelCity, qHotelCity)
                    .where(predicate)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .orderBy(orders.toArray(new OrderSpecifier[0]))
                    .fetch();

            long total = Optional.ofNullable(
                    jpaQueryFactory
                            .select(qHotelPrice.id.count())
                            .from(qHotelPrice)
                            .join(qHotel).on(qHotel.id.eq(qHotelPrice.id.hotelId))
                            .join(qHotel.hotelCity, qHotelCity)
                            .where(predicate)
                            .fetchOne()
            ).orElse(0L);

            return new PageImpl<>(results, pageable, total);
        }
        else {
            List<HotelWithScore> results = jpaQueryFactory
                    .select(
                            Projections.constructor(HotelWithScore.class,
                                    qHotel.id,
                                    qHotel.thumbnailImg,
                                    qHotel.koName,
                                    qHotel.starRating,
                                    qHotel.rankuScore,
                                    qHotel.averageBusinessScore,
                                    qHotel.averageCoupleScore,
                                    qHotel.averageSoloScore,
                                    qHotel.averageFamilyScore,
                                    qHotel.isOnsen
                            ))
                    .from(qHotel)
                    .where(predicate)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .orderBy(orders.toArray(new OrderSpecifier[0]))
                    .fetch();

            long total = Optional.ofNullable(
                    jpaQueryFactory
                            .select(qHotel.id.count())
                            .from(qHotel)
                            .where(predicate)
                            .fetchOne()
            ).orElse(0L);

            return new PageImpl<>(results, pageable, total);
        }

    }

    public Page<PremiumResponse> premiumPage(int location, int sort, int page) {
        Pageable pageable = PageRequest.of(page-1, 20);

        BooleanExpression predicate = this.premiumFilterQueryExpression(location);

        List<OrderSpecifier<?>> orders = new ArrayList<>();


        OrderSpecifier<?> order = this.orderType(sort);
        if (order != null) {
            orders.add(order);
            predicate = predicate.and(qHotel.reviewNum.gt(300).and(qHotel.bestSalePrecent.ne(0.0)));
        }
        orders.add(qHotel.bestCrossedOutRate.desc());
        orders.add(qHotel.starRating.desc());

        List<Hotel> results = jpaQueryFactory
                .selectFrom(qHotel)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orders.toArray(new OrderSpecifier[0]))
                .fetch();

        long total = Optional.ofNullable(
                jpaQueryFactory
                        .select(qHotel.id.count())
                        .from(qHotel)
                        .where(predicate)
                        .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(results.stream().map(PremiumResponse::new).toList(), pageable, total);

    }

    private  OrderSpecifier<?> orderType (int sort) {
        PathBuilder<Hotel> path = new PathBuilder<>(Hotel.class, "hotel");
        return switch (sort) {
            case 2 -> path.getString("averageFamilyScore").desc();
            case 3 -> path.getString("averageBusinessScore").desc();
            case 4 -> path.getString("averageCoupleScore").desc();
            case 5 -> path.getString("averageSoloScore").desc();
            default -> null;
        };
    }


    private BooleanExpression kansaiFilterQueryExpression(BooleanExpression predicate, int location) {

        if (location == 1) {
            return predicate;
        } else if (location < 7) {
            switch (location) {
                case 2: {
                    predicate = predicate.and(qHotel.pointLocation.eq(PointLocation.NAMBA)); break;
                }
                case 3: {
                    predicate = predicate.and(qHotel.pointLocation.eq(PointLocation.UMEDA)); break;
                }
                case 4: {
                    predicate = predicate.and(qHotel.pointLocation.eq(PointLocation.SHINSAIBASHI)); break;
                }
                case 5: {
                    predicate = predicate.and(qHotel.pointLocation.eq(PointLocation.TENOJI)); break;
                }
                case 6: {
                    predicate = predicate.and(qHotel.pointLocation.eq(PointLocation.USJ)); break;
                }
            }
        } else {
            switch (location) {
                case 7: {
                    predicate = predicate.and(qHotel.hotelCity.id.eq(9590L)); break;
                }
                case 8: {
                    predicate = predicate.and(qHotel.hotelCity.id.eq(1784L)); break;
                }
                case 9: {
                    predicate = predicate.and(qHotel.hotelCity.id.eq(5235L)); break;
                }
                case 10: {
                    predicate = predicate.and(qHotel.hotelCity.id.eq(13313L)); break;
                }
            }
        }

        return predicate;
    }
    private BooleanExpression premiumFilterQueryExpression(int location) {
        // 공통 필터 조건
        BooleanExpression common = qHotel.starRating.goe(4.0).and(qHotel.isShow.isTrue());

        BooleanExpression predicate;

        if (location == 1) { return common; }
        predicate = switch (location) {
            case 7 -> qHotel.hotelCity.id.eq(9590L);
            case 8 -> qHotel.hotelCity.id.eq(1784L);
            case 9 -> qHotel.hotelCity.id.eq(5235L);
            case 10 -> qHotel.hotelCity.id.eq(13313L);
            default -> null;
        };


        // predicate가 null이면 그냥 common 리턴
        return (predicate != null) ? predicate.and(common) : common;
    }

    //kyushu==================================
    public Page<HotelWithPrice> kyushuSalePage(int location, int area, int sort, int page, LocalDate searchDate, boolean price) {
        Pageable pageable = PageRequest.of(page-1, 20);
        //
        BooleanExpression predicate = qHotel.prefectureCode.eq(2).and(qHotel.isShow.isTrue());

        //온천판단
        if (sort == 6) {
            predicate = predicate.and(qHotel.isOnsen.isTrue());
        }

        //정렬 null들어가면 에러나서 커버
        List<OrderSpecifier<?>> orders = new ArrayList<>();
        OrderSpecifier<?> order = this.orderType(sort);


        if (searchDate == null) {
            predicate = predicate.and(qHotel.bestDailyRate.ne(0.0));
            predicate = this.kyushuFilterQueryExpression(predicate, location, area);

            if (order != null) {
                orders.add(order);
                predicate = predicate.and(qHotel.reviewNum.gt(300).and(qHotel.bestSalePrecent.ne(0.0)));
            }
            if (price) {
                orders.add(qHotel.bestDailyRate.asc());
                orders.add(qHotel.bestSalePrecent.desc());
            } else {
                orders.add(qHotel.bestSalePrecent.desc());
                orders.add(qHotel.rankuScore.desc());
            }
        } else {
            predicate = predicate.and(qHotelPrice.id.stayDate.eq(searchDate).and(qHotelPrice.dailyRate.ne(0.0)));
            predicate = this.kyushuFilterQueryExpression(predicate, location, area);

            if (price) {
                orders.add(qHotelPrice.dailyRate.asc());
                orders.add(qHotelPrice.salePercent.desc());
                if (order != null) {
                    orders.add(order);
                    predicate = predicate.and(qHotel.reviewNum.gt(300).and(qHotelPrice.salePercent.ne(0.0)));
                }

            } else {
                if (order != null) {
                    orders.add(order);
                    predicate = predicate.and(qHotel.reviewNum.gt(300).and(qHotelPrice.salePercent.ne(0.0)));
                }
                orders.add(qHotelPrice.salePercent.desc());
                orders.add(qHotel.rankuScore.desc());
            }
        }


        if (searchDate != null) {

            List<HotelWithPrice> results = jpaQueryFactory
                    .select(
                            Projections.constructor(HotelWithPrice.class,
                                    qHotel.id,
                                    qHotel.thumbnailImg,
                                    qHotel.koName,
                                    qHotel.starRating,
                                    qHotelPrice.crossedOutRate,
                                    qHotelPrice.dailyRate,
                                    qHotelPrice.salePercent,
                                    qHotel.averageBusinessScore,
                                    qHotel.averageCoupleScore,
                                    qHotel.averageSoloScore,
                                    qHotel.averageFamilyScore,
                                    qHotel.isOnsen
                            ))
                    .from(qHotelPrice)
                    .join(qHotel).on(qHotel.id.eq(qHotelPrice.id.hotelId))
                    .where(predicate)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .orderBy(orders.toArray(new OrderSpecifier[0]))
                    .fetch();

            long total = Optional.ofNullable(
                    jpaQueryFactory
                            .select(qHotelPrice.id.count())
                            .from(qHotelPrice)
                            .join(qHotel).on(qHotel.id.eq(qHotelPrice.id.hotelId))
                            .where(predicate)
                            .fetchOne()
            ).orElse(0L);

            return new PageImpl<>(results, pageable, total);
        }
        else {
            List<HotelWithPrice> results = jpaQueryFactory
                    .select(
                            Projections.constructor(HotelWithPrice.class,
                                    qHotel.id,
                                    qHotel.thumbnailImg,
                                    qHotel.koName,
                                    qHotel.starRating,
                                    qHotel.bestCrossedOutRate,
                                    qHotel.bestDailyRate,
                                    qHotel.bestSalePrecent,
                                    qHotel.averageBusinessScore,
                                    qHotel.averageCoupleScore,
                                    qHotel.averageSoloScore,
                                    qHotel.averageFamilyScore,
                                    qHotel.isOnsen
                            ))
                    .from(qHotel)
                    .where(predicate)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .orderBy(orders.toArray(new OrderSpecifier[0]))
                    .fetch();

            long total = Optional.ofNullable(
                    jpaQueryFactory
                            .select(qHotel.id.count())
                            .from(qHotel)
                            .where(predicate)
                            .fetchOne()
            ).orElse(0L);

            return new PageImpl<>(results, pageable, total);
        }


    }

    public Page<HotelWithScore> kyushuScorePage(int location, int area, int sort, int page, LocalDate searchDate) {
        Pageable pageable = PageRequest.of(page-1, 20);
        //
        BooleanExpression predicate = qHotel.prefectureCode.eq(2).and(qHotel.isShow.isTrue());

        //온천판단
        if (sort == 6) {
            predicate = predicate.and(qHotel.isOnsen.isTrue());
        }

        if (searchDate == null) {
            predicate = predicate.and(qHotel.bestDailyRate.ne(0.0));
            predicate = this.kyushuFilterQueryExpression(predicate, location, area);
        } else {
            predicate = predicate.and(qHotelPrice.id.stayDate.eq(searchDate).and(qHotelPrice.dailyRate.ne(0.0)));
            predicate = this.kyushuFilterQueryExpression(predicate, location, area);
        }

        //정렬 null들어가면 에러나서 커버
        List<OrderSpecifier<?>> orders = new ArrayList<>();
        OrderSpecifier<?> order = this.orderType(sort);

        if (order != null) {
            orders.add(order);
            predicate = predicate.and(qHotel.reviewNum.gt(300).and(qHotel.bestSalePrecent.ne(0.0)));
        }
        orders.add(qHotel.rankuScore.desc());
        orders.add(qHotel.starRating.desc());

        if (searchDate != null) {

            List<HotelWithScore> results = jpaQueryFactory
                    .select(
                            Projections.constructor(HotelWithScore.class,
                                    qHotel.id,
                                    qHotel.thumbnailImg,
                                    qHotel.koName,
                                    qHotel.starRating,
                                    qHotel.rankuScore,
                                    qHotel.averageBusinessScore,
                                    qHotel.averageCoupleScore,
                                    qHotel.averageSoloScore,
                                    qHotel.averageFamilyScore,
                                    qHotel.isOnsen
                            ))
                    .from(qHotelPrice)
                    .join(qHotel).on(qHotel.id.eq(qHotelPrice.id.hotelId))
                    .join(qHotel.hotelCity, qHotelCity)
                    .where(predicate)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .orderBy(orders.toArray(new OrderSpecifier[0]))
                    .fetch();

            long total = Optional.ofNullable(
                    jpaQueryFactory
                            .select(qHotelPrice.id.count())
                            .from(qHotelPrice)
                            .join(qHotel).on(qHotel.id.eq(qHotelPrice.id.hotelId))
                            .join(qHotel.hotelCity, qHotelCity)
                            .where(predicate)
                            .fetchOne()
            ).orElse(0L);

            return new PageImpl<>(results, pageable, total);
        }
        else {
            List<HotelWithScore> results = jpaQueryFactory
                    .select(
                            Projections.constructor(HotelWithScore.class,
                                    qHotel.id,
                                    qHotel.thumbnailImg,
                                    qHotel.koName,
                                    qHotel.starRating,
                                    qHotel.rankuScore,
                                    qHotel.averageBusinessScore,
                                    qHotel.averageCoupleScore,
                                    qHotel.averageSoloScore,
                                    qHotel.averageFamilyScore,
                                    qHotel.isOnsen
                            ))
                    .from(qHotel)
                    .where(predicate)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .orderBy(orders.toArray(new OrderSpecifier[0]))
                    .fetch();

            long total = Optional.ofNullable(
                    jpaQueryFactory
                            .select(qHotel.id.count())
                            .from(qHotel)
                            .where(predicate)
                            .fetchOne()
            ).orElse(0L);

            return new PageImpl<>(results, pageable, total);
        }

    }

    private BooleanExpression kyushuFilterQueryExpression(BooleanExpression predicate, int location, int area) {

        if (location == 1) {
            return predicate;
        } else {

            switch (location) {
                case 2: { //후쿠오카현
                    predicate = predicate.and(qHotel.hotelCity.id.eq(16527L));
                    switch (area) {
                        case 1: {
                            break;
                        }
                        case 2: {//하카타
                            predicate = this.distanceCalculator(predicate, 2);
                            break;
                        }
                        case 3: {//텐진
                            predicate = this.distanceCalculator(predicate, 3);
                            break;
                        }
                        case 4: {//나카스
                            predicate = this.distanceCalculator(predicate, 4);
                            break;
                        }
                    }
                    break;
                }
                case 3: { //오이타현
                    switch (area) {
                        case 1: {
                            predicate = predicate.and(qHotel.hotelCity.id.in(106058L,144L,107890L));
                            break;
                        }
                        case 2: {//유후
                            predicate = predicate.and(qHotel.hotelCity.id.eq(106058L));
                            break;
                        }
                        case 3: {//벳푸
                            predicate = predicate.and(qHotel.hotelCity.id.eq(144L));
                            break;
                        }
                        case 4: {//오이타시
                            predicate = predicate.and(qHotel.hotelCity.id.eq(107890L));
                            break;
                        }
                    }
                    break;
                }
                case 4: { //구마모토 1568
                    predicate = predicate.and(qHotel.hotelCity.id.eq(1568L));
                    break;
                }
                case 5: {//나가사키현
                    switch (area) {
                        case 1: {
                            predicate = predicate.and(qHotel.hotelCity.id.in(193L,107729L,255582L));
                            break;
                        }
                        case 2: {//나가사키시
                            predicate = predicate.and(qHotel.hotelCity.id.eq(193L));
                            break;
                        }
                        case 3: {//사세보
                            predicate = predicate.and(qHotel.hotelCity.id.eq(107729L));
                            break;
                        }
                        case 4: {//운젠시
                            predicate = predicate.and(qHotel.hotelCity.id.eq(255582L));
                            break;
                        }
                    }
                    break;
                }
                case 6: { //가고시마시 6263
                    predicate = predicate.and(qHotel.hotelCity.id.eq(6263L));
                    break;
                }
                case 7: { //미야자키시 13561
                    predicate = predicate.and(qHotel.hotelCity.id.eq(13561L));
                    break;
                }
                case 8: {//사가현
                    switch (area) {
                        case 1: {
                            predicate = predicate.and(qHotel.hotelCity.id.in(8563L,108182L,108162L));
                            break;
                        }
                        case 2: {//사가시
                            predicate = predicate.and(qHotel.hotelCity.id.eq(8563L));
                            break;
                        }
                        case 3: {//우레시노시
                            predicate = predicate.and(qHotel.hotelCity.id.eq(108182L));
                            break;
                        }
                        case 4: {//아리타시
                            predicate = predicate.and(qHotel.hotelCity.id.eq(108162L));
                            break;
                        }
                    }
                    break;
                }
            }
        }

        return predicate;
    }

    private BooleanExpression distanceCalculator (BooleanExpression predicate, int value) {

        double lat = 0.0;
        double lon = 0.0;
        double radiusKm = 2.0;

        switch (value) {
            case 2: {
                lat = 33.59271143123159; lon = 130.43220441665974; break;
            }
            case 3: {
                lat = 33.58052744323853; lon = 130.3892114733367; break;
            }
            case 4: {
                lat = 33.59264691695046; lon = 130.40702134119587; break;
            }
        }
        if ( lat == 0.0 ) {return predicate;}


        // 1차 범위 제한
        double latRange = radiusKm / 111.0;
        double lonRange = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));

        predicate = predicate.and(qHotel.latitude.between(lat - latRange, lat + latRange));
        predicate = predicate.and(qHotel.longitude.between(lon - lonRange, lon + lonRange));

        // 2차 정밀 거리 제한
        NumberExpression<Double> distanceExpr = Expressions.numberTemplate(Double.class,
                "6371 * acos(cos(radians({0})) * cos(radians({1})) * cos(radians({2}) - radians({3})) + sin(radians({0})) * sin(radians({1})))",
                lat, qHotel.latitude, qHotel.longitude, lon);
        predicate = predicate.and(distanceExpr.loe(radiusKm));

        return predicate;
    }

    //detail================================


    public HotelDetailResponse HotelDetail(Long id, LocalDate searchDate) {

        Hotel hotel = jpaQueryFactory
                .selectFrom(qHotel)
                .distinct()
                .leftJoin(qHotel.hotelCity, qHotelCity).fetchJoin()
                .where(qHotel.id.eq(id))
                .fetchOne();

        List<HotelReview> reviews = jpaQueryFactory
                .selectFrom(qHotelReview)
                .where(qHotelReview.hotel.id.eq(id))
                .fetch();

        if (hotel == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "this hotel is deleted: " + id);
        }

        double max = hotel.getAverageBusinessScore();
        int maxLabel = 1;

        if (hotel.getAverageCoupleScore() > max) {
            max = hotel.getAverageCoupleScore();
            maxLabel = 2;
        }
        if (hotel.getAverageSoloScore() > max) {
            max = hotel.getAverageSoloScore();
            maxLabel = 3;
        }
        if (hotel.getAverageFamilyScore() > max) {
            maxLabel = 4;
        }


        String shift_jis = null;
        if ( hotel.getJpName() != null) {
            shift_jis = hotel.getJpName().replaceAll("[ \\t\\n\\x0B\\f\\r]+", "");
            try {
                shift_jis = URLEncoder.encode(shift_jis, "Shift_JIS");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        LocalDate targetDate; int targetCOR; int targetDR; int targetSP;

        if (searchDate != null) {
            HotelPrice hp = jpaQueryFactory
                    .selectFrom(qHotelPrice)
                    .where(qHotelPrice.id.hotelId.eq(id).and(qHotelPrice.id.stayDate.eq(searchDate)))
                    .fetchOne();

            if (hp == null) {throw new ResponseStatusException(HttpStatus.NOT_FOUND, "this hotel is deleted: " + id);}

            targetDate = hp.getStayDate();
            targetCOR = (int)hp.getCrossedOutRate();
            targetDR = (int)hp.getDailyRate();
            targetSP = (int)hp.getSalePercent();


        } else {
            targetDate = hotel.getBestStayDate() != null ? hotel.getBestStayDate() : LocalDate.now();
            targetCOR = (int) hotel.getBestCrossedOutRate();
            targetDR = (int) hotel.getBestDailyRate();
            targetSP = (int) hotel.getBestSalePrecent();

        }


        return HotelDetailResponse.builder()
                .id(hotel.getId())
                .updateDate(hotel.getUpdateDateTime())
                .title(hotel.getTitle())
                .description(hotel.getDescription())
                .keyword(hotel.getKeyword())
                .rankuScore(hotel.getRankuScore())
                .stateName(hotel.getHotelCity().getKoName())
                .reviewSum(hotel.getReviewNum())
                .stateId(hotel.getHotelCity().getId())
                .koName(hotel.getKoName())
                .jpName(shift_jis)
                .enName(hotel.getEnName())
                .address(hotel.getAddress())
                .zipcode(hotel.getZipcode())
                .starRating(hotel.getStarRating())
                .longitude(hotel.getLongitude())
                .latitude(hotel.getLatitude())
                .thumbnailImg(hotel.getThumbnailImg())
                .photo2(hotel.getPhoto2())
                .photo3(hotel.getPhoto3())
                .photo4(hotel.getPhoto4())
                .photo5(hotel.getPhoto5())
                .bestStayDate(targetDate)
                .bestCrossedOutRate(targetCOR)
                .bestDailyRate(targetDR)
                .bestSalePrecent(targetSP)
                .bestLink("https://www.agoda.com/partners/partnersearch.aspx" +
                        "?pcs=1" +
                        "&cid=1950715" +
                        "&hl=ko-kr" +
                        "&hid="+ hotel.getId()
                        +"&checkin="+ targetDate
                        +"&checkout="+ targetDate.plusDays(2)
                        +"&currency=KRW"
                        +"&NumberofAdults=2&NumberofChildren=0&Rooms=1&pcs=6")
                .weekdayPriceList(buildTop5(false, hotel, hotel.getHotelCity()))
                .weekendPriceList(buildTop5(true,  hotel, hotel.getHotelCity()))
                .preferenceValue(maxLabel)
                .averageAllScore((int)(hotel.getAverageAllScore() *10))
                .averageBusinessScore((int)(hotel.getAverageBusinessScore()*10))
                .averageCoupleScore((int)(hotel.getAverageCoupleScore()*10))
                .averageSoloScore((int)(hotel.getAverageSoloScore()*10))
                .averageFamilyScore((int)(hotel.getAverageFamilyScore()*10))
                .brandReviewMap(this.getBrandReviewMap(reviews))
                .build();
    }
    public Map<ReviewBrand, HotelReviewResponse> getBrandReviewMap(List<HotelReview> reviews) {

        if (reviews.isEmpty()) return Collections.emptyMap();
        return reviews.stream()
                .map(HotelReviewResponse::new)
                .collect(Collectors.toMap(
                        HotelReviewResponse::getReviewBrand,
                        Function.identity(),
                        (r1, r2) -> r1.getAllScore() >= r2.getAllScore() ? r1 : r2 // 충돌 시 점수 높은 것 선택
                ));
    }

    private List<HotelPriceResponse> buildTop5(boolean weekend, Hotel hotel, HotelCity hotelCity) {

        Comparator<HotelPriceResponse> DEAL_DESC =
                Comparator.comparingDouble(HotelPriceResponse::getSalePercent)
                        .reversed()
                        .thenComparingDouble(r -> r.getCrossedOutRate() - r.getDailyRate())
                        .reversed()
                        .thenComparing(HotelPriceResponse::getStayDate)
                        .reversed();

        List<HotelPrice> priceList = jpaQueryFactory
                .selectFrom(qHotelPrice)
                .where(qHotelPrice.id.hotelId.eq(hotel.getId()).and(
                        qHotelPrice.dailyRate.ne(0.0)
                ))
                .fetch();

        return priceList.stream()
                .filter(Objects::nonNull)
                .filter(p -> !Double.isNaN(p.getSalePercent()))
                .filter(p -> p.getSalePercent() >= 0)
                .filter(p -> p.isWeekend() == weekend)
                .map(p -> new HotelPriceResponse(p, hotel.getId(), hotelCity.getAsq()))
                .sorted(DEAL_DESC)
                .limit(5)
                .toList();
    }

    //other=================================

    public List<String> getImageList(Long id) {
        Hotel hotel = hotelRepo.findById(id).orElse(null);

        List<String> result = new ArrayList<>();
        result.add(hotel.getThumbnailImg());
        result.add((hotel.getPhoto2()));
        result.add((hotel.getPhoto3()));
        result.add((hotel.getPhoto4()));
        result.add((hotel.getPhoto5()));

        return result;
    }

    //OTU === other=================================
    public AgodaPriceResponse.HotelApiInfo getHotelDateSearcher(long id, LocalDate day) {
        AgodaPriceResponse res = hotelPriceService.callApiForDay(day, day.plusDays(2), Collections.singletonList(id)).block();

        if (res == null || res.getResults() == null || res.getResults().isEmpty()) return null;

        return res.getResults().get(0);
    }

}
