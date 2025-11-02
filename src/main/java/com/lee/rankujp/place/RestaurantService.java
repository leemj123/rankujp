package com.lee.rankujp.place;

import com.lee.rankujp.hotel.cumtom.PointLocation;
import com.lee.rankujp.place.dto.RestaurantResponseDto;
import com.lee.rankujp.place.infra.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantService {

    private final JPAQueryFactory jpaQueryFactory;
    private final QRestaurant qRestaurant = QRestaurant.restaurant;

    @Transactional
    public List<RestaurantResponseDto> getRestaurantLocation(double lat, double lon) {
        double radius = 0.8; // 800m = 0.8km
        double earthRadius = 6371.0; // 지구 반경 (km)
        NumberExpression<Double> distance = Expressions.numberTemplate(Double.class,
                "({0} * acos(cos(radians({1})) * cos(radians({2}.latitude)) * cos(radians({2}.longitude) - radians({3})) + sin(radians({1})) * sin(radians({2}.latitude))))",
                earthRadius, lat, qRestaurant, lon);

        Double C = 4.108587;

        double m = 80.0;

        NumberExpression<Double> R = qRestaurant.rating.coalesce(0.0);
        NumberExpression<Double> v = qRestaurant.userRatingCount.coalesce(0L).doubleValue();

        // 상수들도 NumberExpression으로 명시
        NumberExpression<Double> mConst = Expressions.numberTemplate(Double.class, "{0}", m);
        NumberExpression<Double> cConst = Expressions.numberTemplate(Double.class, "{0}", C);

        // (v / (v + m)) * R + (m / (v + m)) * C
        NumberExpression<Double> score =
                v.divide(v.add(mConst)).multiply(R)
                        .add(
                                mConst.divide(v.add(mConst)).multiply(cConst)
                        );

        List<Restaurant> restaurantList = jpaQueryFactory
                .selectFrom(qRestaurant)
                .where(distance.loe(radius))
                .orderBy(
                        score.desc(), v.desc(), distance.asc()
                )
                .limit(10)
                .fetch();

        List<RestaurantResponseDto> rRDL = new ArrayList<>();
        for (Restaurant restaurant : restaurantList) {
            List<PlaceImg> imgList = restaurant.getPlaceImgList();
            if (imgList == null || imgList.isEmpty()) {continue;}
            PlaceImg place = imgList.get(0);

            rRDL.add(
                    RestaurantResponseDto.builder()
                            .id(restaurant.getId())
                            .thumbnailUri(place.getThumbnailUri())
                            .authorDisplayName(place.getAuthorDisplayName())
                            .authorGoogleMapsUri(place.getGoogleMapsUri())
                            .title(restaurant.getTitle())
                            .titleLanguageCode(restaurant.getTitleLanguageCode())
                            .district(this.equirectangularMeters(lat,lon,restaurant.getLatitude(),restaurant.getLongitude()))
                            .primaryText(restaurant.getPrimaryText())
                            .rating(restaurant.getRating())
                            .userRatingCount(restaurant.getUserRatingCount())
                            .googleMapsUri(restaurant.getGoogleMapsUri())
                    .build()
            );
        }
        return rRDL;
    }

    private int equirectangularMeters(double lat1, double lon1, double lat2, double lon2) {
        double latRad1 = Math.toRadians(lat1);
        double latRad2 = Math.toRadians(lat2);
        double lonRad1 = Math.toRadians(lon1);
        double lonRad2 = Math.toRadians(lon2);

        double x = (lonRad2 - lonRad1) * Math.cos((latRad1 + latRad2) / 2.0);
        double y = (latRad2 - latRad1);
        double dRad = Math.sqrt(x * x + y * y);

        double meters = 6_371_000.0 * dRad;
        return (int) (Math.round(meters / 10.0) * 10.0);
    }

    //=====REST=FUL========================

    @Transactional
    public Page<RestaurantResponseDto> restaurantPage(int location, int sort, int page) {
        Pageable pageable = PageRequest.of(page-1, 20);

        BooleanExpression predicate = this.filterQueryExpression(location);

        Double C = 4.17426;

        double m = 80.0;

        NumberExpression<Double> R = qRestaurant.rating.coalesce(0.0);
        NumberExpression<Double> v = qRestaurant.userRatingCount.coalesce(0L).doubleValue();

        // 상수들도 NumberExpression으로 명시
        NumberExpression<Double> mConst = Expressions.numberTemplate(Double.class, "{0}", m);
        NumberExpression<Double> cConst = Expressions.numberTemplate(Double.class, "{0}", C);

        // (v / (v + m)) * R + (m / (v + m)) * C
        NumberExpression<Double> score =
                v.divide(v.add(mConst)).multiply(R)
                        .add(
                                mConst.divide(v.add(mConst)).multiply(cConst)
                        );

        List<Restaurant> restaurantList = jpaQueryFactory
                .selectFrom(qRestaurant)
                .where(predicate)
                .orderBy( score.desc(), v.desc() )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(
                jpaQueryFactory
                        .select(qRestaurant.id.count())
                        .from(qRestaurant)
                        .where(predicate)
                        .fetchOne()
        ).orElse(0L);

        List<RestaurantResponseDto> rRDL = new ArrayList<>();
        for (Restaurant restaurant : restaurantList) {
            List<PlaceImg> imgList = restaurant.getPlaceImgList();
            if (imgList == null || imgList.isEmpty()) {continue;}
            PlaceImg place = imgList.get(0);

            rRDL.add(
                    RestaurantResponseDto.builder()
                            .id(restaurant.getId())
                            .thumbnailUri(place.getThumbnailUri())
                            .title(restaurant.getTitle())
                            .rating(restaurant.getRating())
                            .userRatingCount(restaurant.getUserRatingCount())
                            .googleMapsUri(restaurant.getGoogleMapsUri())
                            .build()
            );
        }

        return new PageImpl<>(rRDL, pageable, total);
    }

    private BooleanExpression filterQueryExpression(int location) {
        BooleanExpression predicate = qRestaurant.prefectureCode.eq(1);

        if (location == 1) {return predicate;}

        if (location < 7) {
            switch (location) {
                case 2: {
                    predicate = predicate.and(qRestaurant.pointLocation.eq(PointLocation.NAMBA)); break;
                }
                case 3: {
                    predicate = predicate.and(qRestaurant.pointLocation.eq(PointLocation.UMEDA)); break;
                }
                case 4: {
                    predicate = predicate.and(qRestaurant.pointLocation.eq(PointLocation.SHINSAIBASHI)); break;
                }
                case 5: {
                    predicate = predicate.and(qRestaurant.pointLocation.eq(PointLocation.TENOJI)); break;
                }
                case 6: {
                    predicate = predicate.and(qRestaurant.pointLocation.eq(PointLocation.USJ)); break;
                }
            }
        } else {
            switch (location) {
                case 7: {
                    predicate = predicate.and(qRestaurant.hotelCity.id.eq(9590L)); break;
                }
                case 8: {
                    predicate = predicate.and(qRestaurant.hotelCity.id.eq(1784L)); break;
                }
                case 9: {
                    predicate = predicate.and(qRestaurant.hotelCity.id.eq(5235L)); break;
                }
                case 10: {
                    predicate = predicate.and(qRestaurant.hotelCity.id.eq(13313L)); break;
                }
            }
        }

        return predicate;
    }


    //kyushu--------------
    @Transactional
    public Page<RestaurantResponseDto> kyushuRestaurantPage(int location, int area, int sort, int page) {
        Pageable pageable = PageRequest.of(page-1, 20);

        BooleanExpression predicate = this.KyushuFilterQueryExpression(location,area);

        Double C = 4.034879;

        double m = 80.0;

        NumberExpression<Double> R = qRestaurant.rating.coalesce(0.0);
        NumberExpression<Double> v = qRestaurant.userRatingCount.coalesce(0L).doubleValue();

        // 상수들도 NumberExpression으로 명시
        NumberExpression<Double> mConst = Expressions.numberTemplate(Double.class, "{0}", m);
        NumberExpression<Double> cConst = Expressions.numberTemplate(Double.class, "{0}", C);

        // (v / (v + m)) * R + (m / (v + m)) * C
        NumberExpression<Double> score =
                v.divide(v.add(mConst)).multiply(R)
                        .add(
                                mConst.divide(v.add(mConst)).multiply(cConst)
                        );

        List<Restaurant> restaurantList = jpaQueryFactory
                .selectFrom(qRestaurant)
                .where(predicate)
                .orderBy( score.desc(), v.desc() )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(
                jpaQueryFactory
                        .select(qRestaurant.id.count())
                        .from(qRestaurant)
                        .where(predicate)
                        .fetchOne()
        ).orElse(0L);

        List<RestaurantResponseDto> rRDL = new ArrayList<>();
        for (Restaurant restaurant : restaurantList) {
            List<PlaceImg> imgList = restaurant.getPlaceImgList();
            if (imgList == null || imgList.isEmpty()) {continue;}
            PlaceImg place = imgList.get(0);

            rRDL.add(
                    RestaurantResponseDto.builder()
                            .id(restaurant.getId())
                            .thumbnailUri(place.getThumbnailUri())
                            .title(restaurant.getTitle())
                            .rating(restaurant.getRating())
                            .userRatingCount(restaurant.getUserRatingCount())
                            .googleMapsUri(restaurant.getGoogleMapsUri())
                            .build()
            );
        }

        return new PageImpl<>(rRDL, pageable, total);
    }
    private BooleanExpression KyushuFilterQueryExpression(int location, int area) {
        BooleanExpression predicate = qRestaurant.prefectureCode.eq(2);

        if (location == 1) {
            return predicate;
        } else {

            switch (location) {

                case 3: { //오이타현
                    switch (area) {
                        case 1: {
                            predicate = predicate.and(qRestaurant.hotelCity.id.in(106058L,144L,107890L));
                            break;
                        }
                        case 2: {//유후
                            predicate = predicate.and(qRestaurant.hotelCity.id.eq(106058L));
                            break;
                        }
                        case 3: {//벳푸
                            predicate = predicate.and(qRestaurant.hotelCity.id.eq(144L));
                            break;
                        }
                        case 4: {//오이타시
                            predicate = predicate.and(qRestaurant.hotelCity.id.eq(107890L));
                            break;
                        }
                    }
                    break;
                }
                case 4: { //구마모토 1568
                    predicate = predicate.and(qRestaurant.hotelCity.id.eq(1568L));
                    break;
                }
                case 5: {//나가사키현
                    switch (area) {
                        case 1: {
                            predicate = predicate.and(qRestaurant.hotelCity.id.in(193L,107729L,255582L));
                            break;
                        }
                        case 2: {//나가사키시
                            predicate = predicate.and(qRestaurant.hotelCity.id.eq(193L));
                            break;
                        }
                        case 3: {//사세보
                            predicate = predicate.and(qRestaurant.hotelCity.id.eq(107729L));
                            break;
                        }
                        case 4: {//운젠시
                            predicate = predicate.and(qRestaurant.hotelCity.id.eq(255582L));
                            break;
                        }
                    }
                    break;
                }
                case 6: { //가고시마시 1568
                    predicate = predicate.and(qRestaurant.hotelCity.id.eq(1568L));
                    break;
                }
                case 7: { //미야자키시 13561
                    predicate = predicate.and(qRestaurant.hotelCity.id.eq(13561L));
                    break;
                }
                case 8: {//사가현
                    switch (area) {
                        case 1: {
                            predicate = predicate.and(qRestaurant.hotelCity.id.in(8563L,108182L,108162L));
                            break;
                        }
                        case 2: {//사가시
                            predicate = predicate.and(qRestaurant.hotelCity.id.eq(8563L));
                            break;
                        }
                        case 3: {//우레시노시
                            predicate = predicate.and(qRestaurant.hotelCity.id.eq(108182L));
                            break;
                        }
                        case 4: {//아리타시
                            predicate = predicate.and(qRestaurant.hotelCity.id.eq(108162L));
                            break;
                        }
                    }
                    break;
                }
            }
        }

        return predicate;

    }
}
