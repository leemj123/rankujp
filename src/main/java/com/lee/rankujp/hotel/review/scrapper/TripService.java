package com.lee.rankujp.hotel.review.scrapper;

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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class TripService {

    private final WebClient tripWebClient;
    private final HotelRepo hotelRepo;
    private final AnotherReviewTran saver;
    private final JPAQueryFactory jpaQueryFactory;
    private final QHotel qHotel = QHotel.hotel;
    private final QHotelReview qHotelReview = QHotelReview.hotelReview;

    public void startReviewScrap() {
        List<Hotel> target =  jpaQueryFactory
                .selectFrom(qHotel)
                .fetch();


        for (Hotel h : target) {
            try {
                // 1) 네트워크(트랜잭션 밖)
                var doc = tripFlux(h.getEnName())
                        .block(Duration.ofSeconds(10));
                if (doc == null) continue;

                double score = scoreExtraction(doc);
                int count = reviewCountExtraction(doc);

                // 2) 단건 트랜잭션으로 즉시 저장
                saver.insertOne(h, ReviewBrand.TRIPDOTCOM, score, count);

                Thread.sleep(60);

            } catch (Exception e) {

                 log.warn("Hotel {} failed: {}", h.getId(), e.toString());
            }
        }

    }

    private double scoreExtraction(Document document) {
        Element scoreElement = document.selectFirst("span.score-review_score");
        if (scoreElement != null) {
            // 전체 텍스트 → "9.4/10"
            String fullText = scoreElement.text();

            // 앞쪽 숫자만 추출 (공백 기준 또는 "/" 앞까지만 잘라내기)
            String score = fullText.split("/")[0].trim();
            log.info("score: {}", score);
            return Double.parseDouble(score);
        } else {
            log.info("error");
            return 0;
        }
    }

    private int reviewCountExtraction(Document document) {

        Element reviewElement = document.selectFirst("span.score-review_review");

        if (reviewElement != null) {
            String fullText = reviewElement.text();
            int reviewCount = Integer.parseInt(fullText.replaceAll("[^0-9]", ""));
            log.info("score: {}", reviewCount);
            return reviewCount;
        }else {
            log.info("error");
            return 0;
        }

    }

    private Mono<Document> tripFlux(String name) {
        return tripWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("keyword", name) // 자동 인코딩
                        .queryParam("from", "home")
                        .queryParam("Allianceid", "7133519")
                        .queryParam("SID", "262883181")
                        .queryParam("trip_sub1", "rankujp.com")
                        .queryParam("trip_sub3", "D561827")
                        .build())
                .accept(MediaType.TEXT_HTML)
                .header("User-Agent", "Mozilla/5.0 (compatible; RankuBot/1.0)")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(300))
                                .filter(ex -> !(ex instanceof IllegalArgumentException))
                )
                .map(html -> Jsoup.parse(html))
                .onErrorResume(ex -> Mono.empty()); // 실패 시 null 대신 empty
    }
}
