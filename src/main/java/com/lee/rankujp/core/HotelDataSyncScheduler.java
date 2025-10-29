//package com.lee.rankujp.core;
//
//import com.lee.rankujp.hotel.price.HotelPriceService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class HotelDataSyncScheduler {
//
//    private final HotelPriceService hotelPriceService;
//    private final AtomicBoolean running = new AtomicBoolean(false);
//
//    private final JdbcTemplate jdbcTemplate;
//
//    @Scheduled(cron = "0 57 23 * * *", zone = "Asia/Seoul")
//    public void partitionScheduler() {
//        if (!running.compareAndSet(false, true)) {
//            log.warn("파티션 작업 실패, 사이클 할당 실패");
//            return;
//        }
//        try {
//            log.info("롤링 파티셔닝 시작: {}",LocalDateTime.now().plusDays(1));
//
//            String sql = "CALL maintain_hotel_price_partitions(?, ?, DATE(?))";
//            jdbcTemplate.update(sql, "ranku", "hotel_price", java.time.LocalDate.now().plusDays(1));
//
//            log.info("[PartitionMaintenance] hotel_price 파티션 유지 작업 완료: {}", java.time.LocalDate.now().plusDays(1));
//            log.info("롤링 파티셔닝 완료: {}",LocalDateTime.now().plusDays(1));
//        } catch (Exception e) {
//            log.error("파티션 작업 오류: {}", e.getMessage());
//        } finally {
//            running.set(false);
//        }
//
//    }
//
//    @Scheduled(cron = "0 0 0,6,12,18 * * *", zone = "Asia/Seoul")
//    public void runAtFixedTimes() {
//        if (!running.compareAndSet(false, true)) {
//            log.warn("⏳ 이전 작업이 아직 진행 중이어서 이번 사이클은 스킵합니다.");
//            return;
//        }
//
//        log.info("🕕 6시간 스케줄 시작 {}", LocalDateTime.now());
//
//        try {
//            // ① 리액티브 작업이 끝날 때까지 대기
//            hotelPriceService.syncAllPriceWindowBatched()
//                    .doOnSubscribe(s -> log.info("[/agoda] sync started"))
//                    .doOnSuccess(v -> log.info("[/agoda] sync success"))
//                    .doOnError(e -> log.error("[/agoda] sync failed", e))
//                    .timeout(java.time.Duration.ofHours(3))
//                    .block();
//
//            // ③ 동기
//            log.info("② choiceBestScore 시작");
//            hotelPriceService.bestPriceChoicerBatch()
//                    .doOnSubscribe(s -> log.info("[/choiceBeatScore] sync started"))
//                    .doOnSuccess(v -> log.info("[/choiceBeatScore] sync success"))
//                    .doOnError(e -> log.error("[/choiceBeatScore] sync failed", e))
//                    .timeout(java.time.Duration.ofHours(3))
//                    .block();
//            log.info("② choiceBestScore 완료");
//
//            // ② 동기
//            log.info("③ scoreCalculator 시작");
//            hotelPriceService.scoreCalculator();
//            log.info("③ scoreCalculator 완료");
//
//
//
//            log.info("✅ 스케줄 사이클 완료");
//        } catch (Exception e) {
//            log.error("❌ 스케줄 사이클 실패", e);
//        } finally {
//            running.set(false);
//        }
//    }
//
//
//}
