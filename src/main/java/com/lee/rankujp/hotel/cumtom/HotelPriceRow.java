package com.lee.rankujp.hotel.cumtom;

import java.time.LocalDate;

public record HotelPriceRow(
        Long hotelId,
        LocalDate stayDate,
        double crossedOutRate,
        double dailyRate,
        double sailPercent
) {
}
