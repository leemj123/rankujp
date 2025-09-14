package com.lee.rankujp.hotel.price.function;

import com.lee.rankujp.hotel.price.dto.HotelPriceRow;
import com.lee.rankujp.hotel.price.dto.TopBucket;
import reactor.util.function.Tuple2;

import java.util.Map;
import java.util.stream.Collector;

public final class TopKCollectors {
    private TopKCollectors() {}

    // Tuple2<Long, HotelPriceRow> -> Map<Long, TopBucket>
    public static Collector<Tuple2<Long, HotelPriceRow>, Top5ChoiceHotel, Map<Long, TopBucket>> top5WeekdayWeekendPerHotel() {
        return Collector.of(
                Top5ChoiceHotel::new,
                (acc, t) -> acc.accept(t.getT1(), t.getT2()),
                (a, b) -> { a.combine(b); return a; },
                Top5ChoiceHotel::finish
        );
    }
}
