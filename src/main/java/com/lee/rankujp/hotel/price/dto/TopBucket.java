package com.lee.rankujp.hotel.price.dto;

import java.util.List;

public record TopBucket(
        List<HotelPriceRow> weekdayList,
        List<HotelPriceRow> weekendList
) {}
