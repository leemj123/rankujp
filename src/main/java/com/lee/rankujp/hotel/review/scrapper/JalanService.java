package com.lee.rankujp.hotel.review.scrapper;

import com.lee.rankujp.hotel.cumtom.ReviewBrand;
import com.lee.rankujp.hotel.infra.Hotel;
import com.lee.rankujp.hotel.repo.HotelRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JalanService {

    private final WebClient jalanWebClient;
    private final HotelRepo hotelRepo;
    private final AnotherReviewTran saver;

    public void startReviewScrap() {
        List<Hotel> target =  hotelRepo.findAll();
        for (Hotel h : target) {
            try {

                if (h.getJpName() == null) {continue;}

                String cleaned = h.getJpName().replaceAll("[ \\t\\n\\x0B\\f\\r]+", "");
                String encodedName = URLEncoder.encode(cleaned, "Shift_JIS");

                // 1) 네트워크(트랜잭션 밖)
                var doc = jalanFlux(encodedName)
                        .block(java.time.Duration.ofSeconds(10));
                if (doc == null) continue;

                double score = scoreExtraction(doc);
                int count = reviewCountExtraction(doc);

                // 2) 단건 트랜잭션으로 즉시 저장
                saver.insertOne(h, ReviewBrand.JALAN, score, count);

                Thread.sleep(60);

            } catch (Exception e) {

                log.warn("Hotel {} failed: {}", h.getId(), e.toString());
            }
        }

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

   //?keyword=%83J%83%93%83f%83I%83z%83e%83%8B%83Y+%91%E5%8D%E3%82%C8%82%F1%82%CE&distCd=06&rootCd=7701&screenId=FWPCTOP&ccnt=button-fw&image1=

    private Mono<Document> jalanFlux(String name) {

        return jalanWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("keyword", name) // 자동 인코딩
                        .queryParam("rootCd", "7701")
                        .queryParam("screenId", "FWPCTOP")
                        .queryParam("ccnt", "button-fw")
                        .queryParam("image1", "")
                        .build())
                .accept(MediaType.TEXT_HTML)
                .header("User-Agent", "Mozilla/5.0 (compatible; RankuBot/1.0)")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(java.time.Duration.ofSeconds(10))
                .retryWhen(
                        reactor.util.retry.Retry.backoff(2, java.time.Duration.ofMillis(300))
                                .filter(ex -> !(ex instanceof IllegalArgumentException))
                )
                .map(html -> Jsoup.parse(html))
                .onErrorResume(ex -> Mono.empty()); // 실패 시 null 대신 empty
    }
}
