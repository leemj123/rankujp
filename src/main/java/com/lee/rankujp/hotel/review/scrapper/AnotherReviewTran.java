package com.lee.rankujp.hotel.review.scrapper;

import com.lee.rankujp.hotel.cumtom.ReviewBrand;
import com.lee.rankujp.hotel.infra.Hotel;
import com.lee.rankujp.hotel.infra.HotelReview;
import com.lee.rankujp.hotel.repo.HotelReviewRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnotherReviewTran {
    private final HotelReviewRepo hotelReviewRepo;

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 5) // 초단 트랜잭션
    public void insertOne(Hotel hotel, ReviewBrand brand, double score, int reviewCount) {
        HotelReview hr = HotelReview.builder()
                .hotel(hotel)
                .allScore(score)
                .reviewCount(reviewCount)
                .reviewBrand(brand)
                .build();

        hotelReviewRepo.saveAndFlush(hr); // 바로 커밋될 트랜잭션이므로 flush OK
        log.info("200");
    }
}
