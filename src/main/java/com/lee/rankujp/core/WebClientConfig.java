package com.lee.rankujp.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class WebClientConfig {
    @Value("${agoda.key}")
    private String AGODA_KEY;

    @Value("${google.place.key}")
    private String GOOGLE_PLACE_KEY;

    @Bean(name = "agodaApiClient")
    public WebClient agodaApiClient(ObjectMapper om) {
        return WebClient.builder()
                .baseUrl("https://affiliateapi7643.agoda.com")
                .defaultHeaders(headers -> {
                    headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                    headers.set(HttpHeaders.AUTHORIZATION, AGODA_KEY);
                })
                .codecs(c -> {
                    c.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(om, MediaType.APPLICATION_JSON));
                    c.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(om, MediaType.APPLICATION_JSON));
                })
                .build();

    }
    @Bean(name = "agodaWebClient")
    public WebClient agodaWebClient() {
        return WebClient.builder()
                .baseUrl("https://www.agoda.com/api")
                .defaultHeaders(headers -> headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .build();
    }

    @Bean(name = "tripWebClient")
    public WebClient tripWebClient() {
        return WebClient.builder()
                .baseUrl("https://kr.trip.com/global-search/searchlist/search/")
                .defaultHeaders(headers -> headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .build();
    }
    @Bean(name = "jalanWebClient")
    public WebClient jalanWebClient() {
        DefaultUriBuilderFactory f =
                new DefaultUriBuilderFactory("https://www.jalan.net/uw/uwp2011/uww2011init.do");
        // ★ 이미 인코딩된 쿼리스트링을 다시 인코딩하지 않도록
        f.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        return WebClient.builder()
                .uriBuilderFactory(f)
                .defaultHeaders(h -> {
                    h.remove(HttpHeaders.CONTENT_TYPE);
                    h.set(HttpHeaders.ACCEPT_LANGUAGE, "ja-JP,ja;q=0.9");
                })
                .build();
    }
    @Bean(name = "googleWebClient")
    public WebClient googleWebClient() {
        return WebClient.builder()
                .baseUrl("https://places.googleapis.com/v1/places")
                .defaultHeaders(headers -> {
                    headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                    headers.set("X-Goog-Api-Key", GOOGLE_PLACE_KEY);
                })
                .build();
    }
}
