package com.lee.rankujp.hotel.price.function;

import com.lee.rankujp.hotel.price.dto.HotelPriceRow;
import com.lee.rankujp.hotel.price.dto.AgodaPriceResponse;

import java.time.LocalDate;

public final class PriceNormalize {
    private PriceNormalize() {}

    public static HotelPriceRow normalize(LocalDate day, AgodaPriceResponse.HotelApiInfo info) {
        double daily = safePos(info.getDailyRate());
        Double corIn = toNullable(info.getCrossedOutRate());
        Double dpIn  = toNullable(info.getDiscountPercentage());

        double cor, dp;
        if (corIn != null && corIn > 0) {
            if (corIn <= daily) { cor = daily; dp = 0.0; }
            else { cor = corIn; dp = clampPercent(((cor - daily) / cor) * 100.0); }
        } else if (dpIn != null) {
            double dpc = clampPercent(dpIn);
            if (dpc <= 0.0 || dpc >= 100.0) { cor = daily; dp = 0.0; }
            else {
                cor = daily / (1.0 - dpc / 100.0);
                if (!(cor > 0) || cor <= daily || Double.isInfinite(cor) ) {
                    cor = daily; dp = 0.0;
                } else {
                    dp = clampPercent(((cor - daily) / cor) * 100.0);
                }
            }
        } else { cor = daily; dp = 0.0; }

        cor = round2(cor);
        dp  = round2(dp);
        return new HotelPriceRow(day, daily, cor, dp);
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
