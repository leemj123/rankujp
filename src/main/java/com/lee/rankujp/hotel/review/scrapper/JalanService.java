package com.lee.rankujp.hotel.review.scrapper;

import com.lee.rankujp.hotel.cumtom.ReviewBrand;
import com.lee.rankujp.hotel.infra.Hotel;
import com.lee.rankujp.hotel.infra.QHotel;
import com.lee.rankujp.hotel.infra.QHotelReview;
import com.lee.rankujp.hotel.repo.HotelRepo;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JalanService {

    private final WebClient jalanWebClient;
    private final JPAQueryFactory jpaQueryFactory;
    private final QHotel qHotel = QHotel.hotel;
    private final QHotelReview qHotelReview = QHotelReview.hotelReview;
    private final AnotherReviewTran saver;

    public void startReviewScrap() {
        List<Hotel> target =  jpaQueryFactory
                .selectFrom(qHotel)
//                .leftJoin(qHotel.hotelReviewList, qHotelReview)
//                .on(qHotelReview.reviewBrand.eq(ReviewBrand.JALAN))
//                .where(qHotelReview.id.isNull())
                .fetch();

        log.info("str item cnt: {}",target.size());
        for (Hotel h : target) {
            try {

                if (h.getJpName() == null) {continue;}

                String cleaned = h.getJpName().replaceAll("[ \\t\\n\\x0B\\f\\r]+", "");
                String encodedName = URLEncoder.encode(cleaned, "Shift_JIS");

                // 1) 네트워크(트랜잭션 밖)
                Document doc = jalanFlux(encodedName)
                        .block(java.time.Duration.ofSeconds(10));
                if (doc == null) continue;

                double score = scoreExtraction(doc);
                int count = reviewCountExtraction(doc);

                // 2) 단건 트랜잭션으로 즉시 저장
                saver.insertOne(h, ReviewBrand.JALAN, score, count);

                Thread.sleep(1000);

            } catch (Exception e) {

                log.warn("Hotel {} failed: {}", h.getId(), e.toString());
            }
        }


        log.info("fin item cnt: {}",target.size());
    }

    private double scoreExtraction(Document document) {
        // <span class="p-searchResultItem__summaryaverage-num">5.0</span>
        Element scoreElement = document.selectFirst("span.p-searchResultItem__summaryaverage-num");
        if (scoreElement != null) {
            String score = scoreElement.text();

            log.info("score: {}", score);
            return Double.parseDouble(score) * 2;
        } else {
            log.info("error");
            return 0;
        }
    }

    private int reviewCountExtraction(Document document) {

        Element reviewElement = document.selectFirst("span.p-searchResultItem__summarykuchikomi__totalNumber");

        if (reviewElement != null) {
            String fullText = reviewElement.text();   // "5件"
            // 숫자만 남기기
            String onlyNumber = fullText.replaceAll("[^0-9]", ""); // "5"
            try {
                int reviewCount = Integer.parseInt(onlyNumber);
                log.info("score: {}", reviewCount);
                return reviewCount;
            } catch (NumberFormatException e) {
                log.error("parse error: {}", fullText, e);
                return 0;
            }
        } else {
            log.info("error: element not found");
            return 0;
        }

    }

    private Mono<Document> jalanFlux(String encoded) {

        // 쿼리스트링을 직접 붙인다 → WebClient가 재인코딩하지 않음
        String uri = "?keyword=" + encoded;

        return jalanWebClient.get()
                .uri(uri) // baseUrl 뒤에 그대로 붙음 (Postman과 동일 동작)
                .accept(MediaType.TEXT_HTML)
                .header("User-Agent", "Mozilla/5.0 (compatible; RankuBot/1.0)")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(java.time.Duration.ofSeconds(10))
                .retryWhen(reactor.util.retry.Retry.backoff(2, java.time.Duration.ofMillis(300))
                        .filter(ex -> !(ex instanceof IllegalArgumentException)))
                .map(Jsoup::parse)
                .onErrorResume(ex -> Mono.empty());
    }
}
