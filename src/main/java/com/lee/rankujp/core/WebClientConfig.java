package com.lee.rankujp.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean(name = "agodaApiClient")
    public WebClient agodaApiClient() {
        return WebClient.builder()
                .baseUrl("https://affiliateapi7643.agoda.com")
                .defaultHeaders(headers -> headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .build();

    }
    @Bean(name = "agodaWebClient")
    public WebClient agodaWebClient() {
        return WebClient.builder()
                .baseUrl("https://www.agoda.com/api")
                .defaultHeaders(headers -> headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .build();
    }
}
