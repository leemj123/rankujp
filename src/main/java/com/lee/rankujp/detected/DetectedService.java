package com.lee.rankujp.detected;

import com.lee.rankujp.detected.infra.HotelDetailClickLog;
import com.lee.rankujp.detected.infra.HotelDetailClickRepo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DetectedService {
    private final CountryHeaderResolver resolver;
    private final HotelDetailClickRepo hotelDetailClickRepo;

    public void hotelDetected(HttpServletRequest request, int detectedId) {
        String country = resolver.resolveCountry(request);
        hotelDetailClickRepo.save(
                HotelDetailClickLog.builder()
                        .buttonId(detectedId)
                        .country(country)
                        .clickedAt(LocalDateTime.now())
                        .build()
        );
    }
}
