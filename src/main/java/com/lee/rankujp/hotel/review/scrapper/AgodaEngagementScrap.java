package com.lee.rankujp.hotel.review.scrapper;

import com.lee.rankujp.hotel.review.scrapper.dto.AgodaEngagementRequest;
import com.lee.rankujp.hotel.review.scrapper.dto.AgodaEngagementResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AgodaEngagementScrap {

    private final WebClient agodaEngagementClient;


    public Mono<AgodaEngagementResponseDto> callEngagement(LocalDate stayDate, LocalDate finDate, long hotelId) {

        return agodaEngagementClient.post()
                .header("ag-language-locale", "ko-kr")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new AgodaEngagementRequest(stayDate, finDate, Collections.singletonList(hotelId)))
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new IllegalStateException(
                                        "Agoda API error %s: %s".formatted(resp.statusCode(), body)))))
                .bodyToMono(AgodaEngagementResponseDto.class);
    }
}
