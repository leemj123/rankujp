package com.lee.rankujp.hotel.price;

import com.lee.rankujp.hotel.infra.Hotel;
import com.lee.rankujp.hotel.price.dto.AgodaPriceRequest;
import com.lee.rankujp.hotel.price.dto.ImgStarResponse;
import com.lee.rankujp.hotel.repo.HotelRepo;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelInfoUpdater {
    private final HotelPersistService hotelPersistService;
    private final HotelRepo hotelRepo;
    private final WebClient agodaApiClient;
    private static final int MAX_DAY_SHIFT = 45;// 최대 45번(=45일)까지 날짜 이동 시도
    private static final Duration REQ_TIMEOUT = Duration.ofSeconds(15);


    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void imgAndStarUpdate() {
        List<Hotel> hotels = hotelRepo.findAll();
        LocalDate baseStay = LocalDate.now().plusDays(20);


        for (Hotel h : hotels) {
            try {
                ImgStarResponse resp = fetchFirstValidResponse(baseStay, h.getId());
                if (resp == null) {
                    log.warn("No valid response within {} shifts for hotel {}", MAX_DAY_SHIFT, h.getId());
                    continue;
                }
                // ✅ DB 반영만 짧게 새 트랜잭션으로 커밋
                hotelPersistService.saveImgStarInNewTx(h.getId(), resp);
                log.info("{} updated", h.getId());
            } catch (Exception e) {
                log.error("Hotel {} update failed: {}", h.getId(), e.toString(), e);
            }
        }
    }


    /** API 루프는 트랜잭션 밖에서 돌기 */
    private ImgStarResponse fetchFirstValidResponse(LocalDate baseStay, long hotelId) {
        LocalDate stay = baseStay;
        LocalDate fin  = stay.plusDays(2);

        for (int shift = 0; shift < MAX_DAY_SHIFT; shift++) {
            try {
                ImgStarResponse resp = requestAgodaBlocking(stay, fin, hotelId);
                if (resp != null && resp.getError() == null) {
                    return resp; // ✅ 성공하면 바로 리턴
                }
                log.info("Agoda error for hotel {} on {}~{} → shift+1. reason={}",
                        hotelId, stay, fin,
                        resp != null && resp.getError() != null ? resp.getError().getMessage() : "unknown");
            } catch (Exception e) {
                log.warn("Request failed hotel {} on {}~{} → shift+1. err={}",
                        hotelId, stay, fin, e.toString());
            }
            stay = stay.plusDays(1);
            fin  = fin.plusDays(1);
        }
        return null;
    }

    private ImgStarResponse requestAgodaBlocking(LocalDate stayDate, LocalDate finDate, long hotelId) {
        return agodaApiClient.post()
                .uri("/affiliateservice/lt_v1")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new AgodaPriceRequest(stayDate, finDate, hotelId))
                .retrieve()
                .onStatus(HttpStatusCode::isError, r ->
                        r.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new IllegalStateException(
                                        "Agoda API error %s: %s".formatted(r.statusCode(), body)))))
                .bodyToMono(ImgStarResponse.class)
                .timeout(REQ_TIMEOUT)
                .block();
    }
}
