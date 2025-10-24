package com.lee.rankujp.hotel.price.function;

import com.lee.rankujp.hotel.price.dto.HotelPriceRow;
import com.lee.rankujp.hotel.price.dto.AgodaPriceResponse;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public final class PriceNormalize {
    private PriceNormalize() {}

    public static List<HotelPriceRow> normalizeHotelPrice(List<Long> ids, AgodaPriceResponse resp, LocalDate date) {
        final List<AgodaPriceResponse.HotelApiInfo> results =
                (resp != null && resp.getResults() != null) ? resp.getResults() : Collections.emptyList();

        // 응답에 있는 것들
        final Map<Long, HotelPriceRow> byId = results.stream()
                .collect(Collectors.toMap(
                        AgodaPriceResponse.HotelApiInfo::getHotelId,
                        r -> PriceNormalize.priceNormalizeBuilder(r, date),
                        (a, b) -> a, // 중복 키 병합 규칙
                        LinkedHashMap::new
                ));
        // ids에만 있고 응답엔 없는 것들
        for (Long id : ids) {
            if (!byId.containsKey(id)) {
                byId.put(id, new HotelPriceRow(
                        id,
                        date,
                        0.0,
                        0.0,
                        0.0
                ));
            }
        }

        return new ArrayList<>(byId.values());
    }

    private static HotelPriceRow priceNormalizeBuilder(AgodaPriceResponse.HotelApiInfo info, LocalDate date) {
        double dailyRate = safePos(info.getDailyRate());
        Double corIn = toNullable(info.getCrossedOutRate());
        Double dpIn  = toNullable(info.getDiscountPercentage());

        double crossedOutRate, salePercent;
        if (corIn != null && corIn > 0) {
            if (corIn <= dailyRate) {crossedOutRate= dailyRate; salePercent = 0.0; }
            else {crossedOutRate = corIn; salePercent = clampPercent(((crossedOutRate - dailyRate) / crossedOutRate) * 100.0); }
        } else if (dpIn != null) {
            double dpc = clampPercent(dpIn);
            if (dpc <= 0.0 || dpc >= 100.0) {crossedOutRate= dailyRate; salePercent = 0.0; }
            else {
               crossedOutRate= dailyRate / (1.0 - dpc / 100.0);
                if (!(crossedOutRate > 0) ||crossedOutRate<= dailyRate || Double.isInfinite(crossedOutRate) ) {
                   crossedOutRate= dailyRate; salePercent = 0.0;
                } else {
                    salePercent = clampPercent(((crossedOutRate - dailyRate) / crossedOutRate) * 100.0);
                }
            }
        } else {crossedOutRate= dailyRate; salePercent = 0.0; }

        crossedOutRate = round2(crossedOutRate);
        salePercent  = round2(salePercent);

        return new HotelPriceRow(
                info.getHotelId(),
                date,
                dailyRate,
                crossedOutRate,
                salePercent
        );
    }

    private static Double toNullable(double v) { return Double.isNaN(v) ? null : v; }
    private static double safePos(double v) { return (v > 0 && Double.isFinite(v)) ? v : 0.0; }
    private static double clampPercent(double p) {
        if (!Double.isFinite(p)) return 0.0;
        if (p < 0) return 0.0;
        if (p > 100) return 100.0;
        return p;
    }
    private static double round2(double v) { return Math.round(v * 100.0) / 100.0; }
}
