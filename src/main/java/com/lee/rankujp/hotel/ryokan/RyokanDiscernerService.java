//package com.lee.rankujp.hotel.ryokan;
//
//
//import com.lee.rankujp.hotel.infra.Hotel;
//import com.lee.rankujp.hotel.infra.QHotel;
//import com.lee.rankujp.hotel.repo.HotelRepo;
//import com.querydsl.jpa.impl.JPAQueryFactory;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatusCode;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class RyokanDiscernerService {
//
//    private final JPAQueryFactory jpaQueryFactory;
//    private final QHotel qHotel = QHotel.hotel;
//    private final WebClient agodaEngagementClient;
//
//    @Transactional
//    public long ryokanDiscerner() {
//        List<Hotel> hotels = jpaQueryFactory
//                .selectFrom(qHotel)
//                .where(qHotel.isOnsen.isNull())
//                .limit(500)
//                .offset(0)
//                .fetch();
//        LocalDateTime now = LocalDateTime.now();
//        LocalDate nowDate = now.toLocalDate();
//
//        if (hotels.isEmpty()) {return 0;}
//
//        log.info("list cnt: {}",hotels.size());
//
//        List<Long> failIdList =  new ArrayList<>();
//        for (Hotel h : hotels) {
//            RyokanGraphQLResponse ryokanInfo = sender(new RyokanRequest(h.getId(),now,nowDate,nowDate.plusDays(2))).block();
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                e.printStackTrace();
//            }
//
//            if (ryokanInfo == null) {
//                log.info("fail id: {}", h.getId());
//                failIdList.add(h.getId());
//                continue;
//            }
//            List<RyokanGraphQLResponse.FeatureGroup> featureGroups = ryokanInfo.getData().getPropertyDetailsSearch().getPropertyDetails().get(0).getContentDetail().getContentFeatures().getFeatureGroups();
//
//            List<RyokanGraphQLResponse.Feature> features = featureGroups.stream()
//                    .filter(fg ->  fg.getId() == 35)
//                    .flatMap(fg -> fg.getFeatures().stream())
//                    .toList();
//
//            boolean hasTarget = features.stream()
//                    .anyMatch(f -> f.getId() == 104);
//            log.info("id: {} / value: {}",h.getId(), hasTarget);
//            h.onsenUpdate(hasTarget);
//
//        }
//
//        return failIdList.size();
//    }
//
//    private Mono<RyokanGraphQLResponse> sender(RyokanRequest ryokanRequest) {
//        return agodaEngagementClient.post()
//                .header("ag-language-locale", "ko-kr")
//                .bodyValue(ryokanRequest)
//                .retrieve()
//                .onStatus(HttpStatusCode::isError, r ->
//                        r.bodyToMono(String.class)
//                                .flatMap(body -> Mono.error(new IllegalStateException(
//                                        "Agoda API error %s: %s".formatted(r.statusCode(), body)))))
//                .bodyToMono(RyokanGraphQLResponse.class);
//
//    }
//
//    @Transactional
//    public void goSave(List<PostmanSave> ids) {
//        Map<Long,Boolean> map = new HashMap<>();
//
//        ids.stream().forEach(item -> {map.put(item.getId(), item.isValue());});
//
//        List<Long> sqlIds = ids.stream().map(PostmanSave::getId).toList();
//
//        List<Hotel> h = jpaQueryFactory
//                .selectFrom(qHotel)
//                .where(qHotel.id.in(sqlIds))
//                .fetch();
//
//        for (Hotel h1 : h) {
//            h1.onsenUpdate(map.get(h1.getId()));
//        }
//    }
//}
