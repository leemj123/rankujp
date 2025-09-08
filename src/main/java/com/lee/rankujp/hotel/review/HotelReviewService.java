package com.lee.rankujp.hotel.review;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class HotelReviewService {

    private final WebClient ReviewWebClient;
    public void idScrapper(long id) {


    }
}
