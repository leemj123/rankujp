package com.lee.rankujp.place;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lee.rankujp.hotel.infra.Hotel;
import com.lee.rankujp.hotel.infra.QHotel;
import com.lee.rankujp.hotel.repo.HotelRepo;
import com.lee.rankujp.place.dto.GooglePlaceRequest;
import com.lee.rankujp.place.dto.GoogleRestaurantResponse;
import com.lee.rankujp.place.dto.GoogleRestaurantWrapper;
import com.lee.rankujp.place.dto.HotelLocationPoint;
import com.lee.rankujp.place.infra.*;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.netty.handler.timeout.TimeoutException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantService {

    private final WebClient googleWebClient;
    private final HotelRepo hotelRepo;
    private final RestaurantRepo restaurantRepo;
    private final PlaceImgRepo placeImgRepo;

    private final JPAQueryFactory jpaQueryFactory;
    private final QHotel qHotel = QHotel.hotel;
    private final QRestaurant qRestaurant = QRestaurant.restaurant;



    public List<Long> addRestaurant(long lastId) {

        List<HotelLocationPoint> pointList = jpaQueryFactory
                .select(Projections.constructor(
                        HotelLocationPoint.class,
                        qHotel.id,
                        qHotel.latitude,
                        qHotel.longitude
                ))
                .from(qHotel)
                .limit(500)
                .where(qHotel.id.gt(lastId))
                .fetch();

//        List<HotelLocationPoint> pointList = jpaQueryFactory
//                .select(Projections.constructor(
//                        HotelLocationPoint.class,
//                        qHotel.id,
//                        qHotel.latitude,
//                        qHotel.longitude
//                ))
//                .from(qHotel)
//                .where(qHotel.id.in())
//                .fetch();

        List<Long> failIdList = new ArrayList<>();
        for (HotelLocationPoint hlp : pointList) {
            try {
                GooglePlaceRequest googlePlaceRequest = new GooglePlaceRequest(hlp.getLatitude(), hlp.getLongitude());
                GoogleRestaurantWrapper googleRestaurantWrapper = this.googlePlaceFlux(googlePlaceRequest).block();
                if (googleRestaurantWrapper == null) {log.info("googlePlacesWrapper is null"); continue;}


                List<GoogleRestaurantResponse> allPlaces = googleRestaurantWrapper.getPlaces();
                if (allPlaces == null || allPlaces.isEmpty()) {log.info("googlePlacesWrapper.getPlaces() is empty"); continue;}


                // 1️⃣ 모든 Google ID 추출
                List<String> googleIdCheckList = allPlaces.stream()
                        .map(GoogleRestaurantResponse::getId)
                        .toList();

                // 2️⃣ DB에 이미 존재하는 Google ID 목록 조회
                List<String> existGoogleIdList = jpaQueryFactory
                        .select(qRestaurant.googlePlaceId)
                        .from(qRestaurant)
                        .where(qRestaurant.googlePlaceId.in(googleIdCheckList))
                        .fetch();

                // 3️⃣ DB에 없는 것만 필터링
                Set<String> existIdSet = new HashSet<>(existGoogleIdList);

                List<GoogleRestaurantResponse> filteredPlaces = allPlaces.stream()
                        .filter(place -> !existIdSet.contains(place.getId()))
                        .toList();

                for (GoogleRestaurantResponse grr : filteredPlaces) {
                    Restaurant restaurant = new Restaurant(grr);

                    var photos = grr.getPhotos();
                    if (photos != null && !photos.isEmpty()) {

                        for (GoogleRestaurantResponse.Photo p : photos) {
                            restaurant.addImage(new PlaceImg(p));
                        }
                    }

                    restaurantRepo.save(restaurant);
                }
            } catch (Exception e) {
                log.warn("FILL : {}",hlp.getId());
                failIdList.add(hlp.getId());
            } finally {
                log.info("id : {}",hlp.getId());
            }
        }
        return failIdList;
    }

    private Mono<GoogleRestaurantWrapper> googlePlaceFlux(GooglePlaceRequest gpr) {


        return googleWebClient.post()
                .accept(MediaType.APPLICATION_JSON)
                .header("User-Agent", "Mozilla/5.0 (compatible; RankuBot/1.0)")
                .bodyValue(gpr)
                .retrieve()

                // ✅ 403 Forbidden → 크레딧 만료 로그 찍고 전체 종료
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    if (response.statusCode().value() == 403) {
                                        log.error("🚫 Google Places API 403 Forbidden - 크레딧 종료로 판단됨. body={}", body);
                                        // 전체 로직을 종료시키기 위해 에러를 throw
                                        return Mono.error(new RuntimeException("Google API credit expired"));
                                    }
                                    return Mono.error(new RuntimeException("Google API client error: " + body));
                                })
                )

                // ✅ 응답이 정상일 때 바디를 파싱
                .bodyToMono(GoogleRestaurantWrapper.class)

                // ✅ 응답이 아무것도 없을 때 → null 반환
                .switchIfEmpty(Mono.justOrEmpty(null))

                .onErrorResume(ex -> {
                    log.error("❌ Google Places 요청 실패 [{}]: {}",
                            ex.getClass().getSimpleName(), ex.getMessage(), ex);

                    if (ex instanceof WebClientResponseException responseEx) {
                        log.error("🌐 WebClientResponseException");
                        log.error("  ↳ Status : {}", responseEx.getStatusCode());
                        log.error("  ↳ Headers: {}", responseEx.getHeaders());
                        log.error("  ↳ Body   : {}", responseEx.getResponseBodyAsString());
                    }
                    else if (ex instanceof WebClientRequestException requestEx) {
                        log.error("🌐 WebClientRequestException");
                        log.error("  ↳ URL     : {}", requestEx.getUri());
                        log.error("  ↳ Cause   : {}", requestEx.getCause() != null ? requestEx.getCause().getMessage() : "none");
                    }
                    else if (ex instanceof TimeoutException) {
                        log.error("⏰ TimeoutException: 요청 제한 시간 초과");
                    }
                    else {
                        log.error("⚠️ 기타 예외 발생: {}", ex.toString());
                    }

                    // null 대신 빈 Mono 반환
                    return Mono.empty();
                })

                // ✅ 타임아웃
                .timeout(Duration.ofSeconds(10));
    }

}
