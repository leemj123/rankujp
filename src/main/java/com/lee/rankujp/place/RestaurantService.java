package com.lee.rankujp.place;

import com.lee.rankujp.hotel.infra.QHotel;
import com.lee.rankujp.place.dto.RestaurantResponseDto;
import com.lee.rankujp.place.infra.*;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


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

        List<Restaurant> restaurantList = jpaQueryFactory
                .selectFrom(qRestaurant)
                .where(distance.loe(radius))
                .orderBy(distance.asc())
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
}
