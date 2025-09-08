package com.lee.rankujp.hotel.repo;

import com.lee.rankujp.hotel.infra.HotelReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelReviewRepo extends JpaRepository<HotelReview, Long> {
}
