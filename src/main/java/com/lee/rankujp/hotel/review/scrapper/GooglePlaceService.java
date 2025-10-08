package com.lee.rankujp.hotel.review.scrapper;

import com.lee.rankujp.hotel.cumtom.ReviewBrand;
import com.lee.rankujp.hotel.infra.Hotel;
import com.lee.rankujp.hotel.infra.QHotel;
import com.lee.rankujp.hotel.infra.QHotelReview;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GooglePlaceService {

    private final WebClient googleWebClient;
    private final JPAQueryFactory jpaQueryFactory;
    private final QHotel qHotel = QHotel.hotel;
    private final QHotelReview qHotelReview = QHotelReview.hotelReview;
    private final AnotherReviewTran saver;

    public void startReviewScrap() {
        List<Hotel> target =  jpaQueryFactory
                .selectFrom(qHotel)
                .leftJoin(qHotel.hotelReviewList, qHotelReview)
                .on(qHotelReview.reviewBrand.eq(ReviewBrand.GOOGLE))
                .where(qHotelReview.id.isNull())
                .fetch();

        log.info("str item cnt: {}",target.size());
        for (Hotel h : target) {
            try {

                if (h.getEnName() == null) {continue;}

                // 1) 네트워크(트랜잭션 밖)
                String hotelGoogleId = googleIdFlux(h.getEnName()).block(java.time.Duration.ofSeconds(10));
                GooglePlaceReviewDto googlePlaceReviewDto = googlePlaceFlux(hotelGoogleId).block(java.time.Duration.ofSeconds(10));
                if (googlePlaceReviewDto == null) {
                    log.warn("받아올 수 없음: {}", h.getId());
                    continue;
                }

                double score = Math.floor((googlePlaceReviewDto.getRating() * 2) * 10) / 10.0;

                log.info("score : {}",score);
                log.info("count : {}",googlePlaceReviewDto.getUserRatingCount());

                // 2) 단건 트랜잭션으로 즉시 저장
                saver.insertOne(h, ReviewBrand.GOOGLE, score, googlePlaceReviewDto.getUserRatingCount());

                Thread.sleep(1000);

            } catch (Exception e) {
                log.warn("Hotel {} failed: {}", h.getId(), e.toString());
            }
        }


        log.info("fin item cnt: {}",target.size());
    }

    private Mono<String> googleIdFlux(String name) {

        return googleWebClient.post()
                .uri(":searchText") // baseUrl 뒤에 그대로 붙음 (Postman과 동일 동작)
                .header("X-Goog-FieldMask", "places.id")
                .accept(MediaType.APPLICATION_JSON)
                .header("User-Agent", "Mozilla/5.0 (compatible; RankuBot/1.0)")
                .bodyValue(Map.of("textQuery", name))
                .retrieve()
                .bodyToMono(GooglePlaceResponseDto.class)
                .timeout(Duration.ofSeconds(10))
                .retryWhen(reactor.util.retry.Retry.backoff(2, Duration.ofMillis(300))
                        .filter(ex -> !(ex instanceof IllegalArgumentException)))
                .map(r -> {
                    if (r == null || r.getPlaces() == null || r.getPlaces().isEmpty()) {
                        return null; // 결과가 없을 경우 null 반환
                    }
                    return r.getPlaces().get(0).getId(); // 첫 번째 place의 id 반환
                })
                .onErrorResume(ex -> Mono.empty()); // 오류 시 빈 Mono
    }
    private Mono<GooglePlaceReviewDto> googlePlaceFlux(String id) {

        return googleWebClient.get()
                .uri("/"+id)
                .accept(MediaType.APPLICATION_JSON)
                .header("X-Goog-FieldMask", "rating,userRatingCount")
                .header("User-Agent", "Mozilla/5.0 (compatible; RankuBot/1.0)")
                .retrieve()
                .bodyToMono(GooglePlaceReviewDto.class)
                .timeout(Duration.ofSeconds(10))
                .retryWhen(reactor.util.retry.Retry.backoff(2, Duration.ofMillis(300))
                        .filter(ex -> !(ex instanceof IllegalArgumentException)))

                .onErrorResume(ex -> Mono.empty()); // 오류 시 빈 Mono
    }
}
