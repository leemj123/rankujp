package com.lee.rankujp.core;

import com.lee.rankujp.hotel.mvc.service.HotelService;
import com.lee.rankujp.hotel.price.HotelPriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class HotelDataSyncScheduler {

    private final HotelService hotelService;
    private final HotelPriceService hotelPriceService;
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Scheduled(cron = "0 0 0,6,12,18 * * *", zone = "Asia/Seoul")
    public void runAtFixedTimes() {
        if (!running.compareAndSet(false, true)) {
            log.warn("⏳ 이전 작업이 아직 진행 중이어서 이번 사이클은 스킵합니다.");
            return;
        }

        log.info("🕕 6시간 스케줄 시작 {}", LocalDateTime.now());

        try {
            // ① 리액티브 작업이 끝날 때까지 대기
            hotelPriceService.syncAllPriceWindowBatched()
                    .doOnSubscribe(s -> log.info("[/agoda] sync started"))
                    .doOnSuccess(v -> log.info("[/agoda] sync success"))
                    .doOnError(e -> log.error("[/agoda] sync failed", e))
                    .timeout(java.time.Duration.ofHours(3))
                    .block();

            // ② 동기
            log.info("② scoreCalculator 시작");
            hotelService.scoreCalculator();
            log.info("② scoreCalculator 완료");

            // ③ 동기
            log.info("③ choiceBeatScore 시작");
            hotelService.choiceBeatScore();
            log.info("③ choiceBeatScore 완료");

            log.info("✅ 스케줄 사이클 완료");
        } catch (Exception e) {
            log.error("❌ 스케줄 사이클 실패", e);
        } finally {
            running.set(false);
        }
    }
}
