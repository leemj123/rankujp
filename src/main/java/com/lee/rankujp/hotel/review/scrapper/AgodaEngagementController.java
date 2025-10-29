package com.lee.rankujp.hotel.review.scrapper;

import com.lee.rankujp.hotel.review.scrapper.dto.AgodaEngagementResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class AgodaEngagementController {

    private final AgodaEngagementScrap agodaEngagementScrap;

    @GetMapping("/odongdev/engagement")
    public String hotelEngagement(@RequestParam LocalDate stayDate, @RequestParam LocalDate finDate, @RequestParam Long hotelId) {
        AgodaEngagementResponseDto response = agodaEngagementScrap
                .callEngagement(stayDate, finDate, hotelId)
                .block();

        if (response == null
                || response.getData() == null
                || response.getData().getPropertyDetailsSearch() == null
                || response.getData().getPropertyDetailsSearch().getPropertyDetails() == null
                || response.getData().getPropertyDetailsSearch().getPropertyDetails().isEmpty()) {
            return null; // 예외 상황 시 기본값
        }

        return response.getData()
                .getPropertyDetailsSearch()
                .getPropertyDetails()
                .get(0)
                .getContentDetail()
                .getContentEngagement()
                .getTodayBooking()
                .replaceAll("[^0-9]", ""); // "오늘 4회 예약됨" → "4"
    }
}
