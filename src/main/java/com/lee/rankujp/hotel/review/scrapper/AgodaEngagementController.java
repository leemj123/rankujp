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

        try {

            return response.getData()
                    .getPropertyDetailsSearch()
                    .getPropertyDetails()
                    .get(0)
                    .getContentDetail()
                    .getContentEngagement()
                    .getTodayBooking()
                    .replaceAll("[^0-9]", "");

        } catch (NullPointerException e) {
            return null;
        }
    }
}
