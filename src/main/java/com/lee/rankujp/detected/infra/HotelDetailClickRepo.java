package com.lee.rankujp.detected.infra;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelDetailClickRepo extends JpaRepository<HotelDetailClickLog, Long> {
}
