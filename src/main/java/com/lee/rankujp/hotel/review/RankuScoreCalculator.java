package com.lee.rankujp.hotel.review;

import com.lee.rankujp.hotel.infra.Hotel;


public final class RankuScoreCalculator {
    private RankuScoreCalculator() {}

    // ====== 가중치(총합 100) ======
    private static final int W_REVIEW = 40;
    private static final int W_PRICE  = 30;
    private static final int W_MEDIA  = 15;
    private static final int W_STAR   = 15;

    // ====== 리뷰 스무딩 파라미터 ======
    // 리뷰 평균(0~10)을 0~100으로 맵핑하기 전에, 베이지안 스무딩
    private static final double PRIOR_MEAN_REVIEW_10PT = 7.8; // 사이트 전체 평균 가정(조정 가능)
    private static final double PRIOR_WEIGHT = 50.0;          // 50개 리뷰만큼의 신뢰도(조정 가능)

    // 리뷰 수 포화용 pivot: ln(1+n)/ln(1+pivot) <= 1
    private static final long REVIEW_PIVOT = 500;             // 500개에서 사실상 상한에 근접

    // 신생 호텔 하한 보정
    private static final int MIN_FLOOR_FOR_NEW_HOTEL = 55;    // 신생/저리뷰 시 최소 보장 바닥점
    private static final long LOW_REVIEW_THRESHOLD = 20;      // 20개 미만이면 바닥 보정 고려

    // 사진/콘텐츠
    private static final int MAX_PHOTO_COUNT = 5;             // 썸네일+보조 4장 기준

    // 가격 매력도: bestSalePercent(%)를 0~100 점수로
    private static final double PRICE_MAX_BENCHMARK = 50.0;   // 50% 이상은 포화로 간주
    private static final double PRICE_LOG_SOFTENER = 0.35;    // 과도한 큰 할인의 급증을 완화

    public static int hotelScore(Hotel h) {
        // 안전값
        double avg10 = safe(h.getAverageAllScore(), 10.0); // 0~10
        long reviewCount = Math.max(0, h.getReviewNum());
        double star = safe(h.getStarRating(), 5.0);        // 0~5 가정
        double bestSalePercent = Math.max(0.0, h.getBestSalePrecent()); // 0~∞

        // 1) 리뷰 품질 (0~100)
        // 베이지안 평균 (0~10) -> (0~100)
        double bayesMean10 = bayesianMean(avg10, reviewCount);
        double baseReviewScore = (bayesMean10 / 10.0) * 100.0;

        // 리뷰 수 가점: ln(1+n)/ln(1+REVIEW_PIVOT)
        double volumeBoost = saturatingLog(reviewCount); // 0~1
        // 가점은 최대 +10점까지 부여 (리뷰 수가 많을수록 신뢰도 반영)
        double reviewScore = clamp(baseReviewScore + 10.0 * volumeBoost, 0, 100);

        // 2) 가격 매력도 (0~100)
        // 할인율 0~PRICE_MAX_BENCHMARK% 구간을 주된 스케일로 보고, 그 이상은 포화
        double capped = Math.min(bestSalePercent, PRICE_MAX_BENCHMARK);
        // 부드러운 증가: (x/bench)^(1 - PRICE_LOG_SOFTENER)
        double priceNorm = Math.pow(capped / PRICE_MAX_BENCHMARK, (1.0 - PRICE_LOG_SOFTENER));
        double priceScore = clamp(priceNorm * 100.0, 0, 100);

        // 3) 사진/콘텐츠 (0~100)
        int photoCount = 0;
        if (h.getThumbnailImg() != null) photoCount++;
        if (h.getPhoto2() != null) photoCount++;
        if (h.getPhoto3() != null) photoCount++;
        if (h.getPhoto4() != null) photoCount++;
        if (h.getPhoto5() != null) photoCount++;
        double mediaScore = (Math.min(photoCount, MAX_PHOTO_COUNT) * 1.0 / MAX_PHOTO_COUNT) * 100.0;

        // 4) 공식 등급/운영 품질(0~5 -> 0~100)
        double starScore = (star / 5.0) * 100.0;

        // 가중 합산
        double weighted =
                reviewScore * (W_REVIEW / 100.0) +
                        priceScore  * (W_PRICE  / 100.0) +
                        mediaScore  * (W_MEDIA  / 100.0) +
                        starScore   * (W_STAR   / 100.0);

        int finalScore = (int) Math.round(clamp(weighted, 0, 100));

        // === 신생 호텔/저리뷰 바닥 보정 ===
        // 리뷰가 매우 적거나(LOW_REVIEW_THRESHOLD 미만) 오픈일이 최근(18개월)이면
        // 과도한 페널티를 주지 않도록 최소 바닥점 보장(단, 할인/사진/등급이 최악이면 바닥에도 못 미칠 수 있음)
        if (reviewCount < LOW_REVIEW_THRESHOLD) {
            finalScore = Math.max(finalScore, MIN_FLOOR_FOR_NEW_HOTEL);
        }

        return finalScore;
    }

    // ====== Helpers ======
    private static double bayesianMean(double mean, long n) {
        return ((mean * n) + (RankuScoreCalculator.PRIOR_MEAN_REVIEW_10PT * RankuScoreCalculator.PRIOR_WEIGHT)) / (n + RankuScoreCalculator.PRIOR_WEIGHT);
    }

    private static double saturatingLog(long n) {
        if (n <= 0) return 0.0;
        double denom = Math.log1p(RankuScoreCalculator.REVIEW_PIVOT);
        if (denom <= 0) return 1.0;
        return Math.min(1.0, Math.log1p(n) / denom);
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private static double safe(Double v, double max) {
        if (v == null || Double.isNaN(v) || Double.isInfinite(v)) return 0.0;
        return clamp(v, 0.0, max);
    }

}
