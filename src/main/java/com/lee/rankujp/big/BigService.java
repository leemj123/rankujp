//package com.lee.rankujp.big;
//
//import com.lee.rankujp.hotel.infra.QHotel;
//import com.lee.rankujp.hotel.infra.QHotelCity;
//import com.lee.rankujp.hotel.repo.HotelRepo;
//import com.lee.rankujp.hotel.review.HotelReviewService;
//import com.lee.rankujp.hotel.review.scrapper.JalanService;
//import com.lee.rankujp.hotel.review.scrapper.TripService;
//import com.querydsl.jpa.impl.JPAQueryFactory;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Mono;
//
//import java.util.*;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class BigService {
//
//    private final JPAQueryFactory jpaQueryFactory;
//    private final QAgodaJa qAgodaJa = QAgodaJa.agodaJa;
//    private final QAgodaKo qAgodaKo = QAgodaKo.agodaKo;
//    private final QAgodaCity qAgodaCity = QAgodaCity.agodaCity;
//    private final QHotelCity qHotelCity = QHotelCity.hotelCity;
//    private final QHotel qHotel = QHotel.hotel;
//    private final HotelRepo hotelRepo;
//
//    private final HotelReviewService hotelReviewService;
//    private final TripService tripService;
//    private final JalanService jalanService;
//
//
////    @Transactional
////    public void citySeparate() {
////        List<AgodaCity> entityList = jpaQueryFactory
////                .selectFrom(qAgodaCity)
////                .fetch();
////
////        for (AgodaCity e : entityList) {
////            Long count = jpaQueryFactory
////                    .select(qAgodaKo.hotel_id.count())
////                    .from(qAgodaKo)
////                    .where(qAgodaKo.city_id.eq(e.getCityId()))
////                    .fetchOne();
////
////            e.setCount(count != null ? count : 0L);
////
////        }
////    }
////
////    public void firstQueue() {
////        List<HotelCity> cityList = jpaQueryFactory
////                .selectFrom(qHotelCity)
////                .where( qHotelCity.id.in(
////                        144,
////                        193,
////                        1568,
////                        6263,
////                        8563,
////                        13561,
////                        16527,
////                        106058,
////                        106491,
////                        107729,
////                        107890,
////                        108162,
////                        108182,
////                        255582
////                ))
////                .fetch();
////        List<Hotel> hotelList = new ArrayList<>();
////
////        for (HotelCity e : cityList) {
////
////            List<AgodaKo> eList = jpaQueryFactory
////                    .selectFrom(qAgodaKo)
////                    .where(qAgodaKo.city_id.eq(e.getId()))
////                    .fetch();
////
////            hotelList.addAll(eList.stream().map(p -> new Hotel(p, e)).toList());
////        }
////
////        hotelRepo.saveAll(hotelList);
////    }
//
//    public Mono<Void> secondQueue() {
//        //아고다
////        Mono<Void> run = hotelReviewService.syncAllReviews()
////                .doOnSubscribe(s -> log.info("[/agoda] sync started"))
////                .doOnError(e -> log.error("[/agoda] sync failed", e))
////                .doOnSuccess(v -> log.info("[/agoda] sync completed"))
////                .doFinally(sig -> log.info("[/agoda] finally: {}", sig));
////
////        run.subscribe();
////
////        tripService.startReviewScrap();
//        jalanService.startReviewScrap();
//
//        return Mono.just("fin").then();
//    }
////    @Transactional
////    public void jpInsert() {
////        List<AgodaJa> agodaJaList = jpaQueryFactory.selectFrom(qAgodaJa).where(qAgodaJa.hotel_id.gt(10540643L)).fetch();
////        List<Hotel> hotelList = jpaQueryFactory.selectFrom(qHotel).where(qHotel.id.gt(10540643L)).fetch();
////
////        Map<Long, Hotel> hotelMap = new HashMap<>();
////
////        for (Hotel h : hotelList) {
////            hotelMap.put(h.getId(), h);
////        }
////
////        for (AgodaJa agodaJa : agodaJaList) {
////            Hotel hotel = hotelMap.get(agodaJa.getHotel_id());
////            if (hotel == null) {
////                log.info("404 : {}", agodaJa.getHotel_id());
////                continue;
////            }
////            hotel.setJpName(agodaJa.getHotel_translated_name());
////        }
////    }
//
////    @Transactional
////    public void updater() {
////
////        List<Hotel> hotels = jpaQueryFactory
////                .selectFrom(qHotel)
////                .where(qHotel.title.isNull()
////                        .and(qHotel.koName.isNotNull())
////                )
////                .fetch();
////
////        hotels.forEach(this::setSEO);
////
////    }
////    private static final int TITLE_MAX = 60;
////    private static final int DESC_MAX  = 160;
////
////    private static final List<String> KEYWORD_POOL = List.of(
////            "일본호텔 순위","호텔 가격 비교","호텔 할인","예약사이트","호텔 예약 꿀팁",
////            "일본호텔추천","일본호텔순위","일본호텔랭킹","일본호텔정보","일본호텔예약","일본호텔비교"
////    );
////
////    private void setSEO(Hotel h) {
////        this.setDescription(h);
////        this.setTitle(h);
////        this.setKeywords(h);
////    }
////    /** Title 생성 */
////    private void setTitle(Hotel h) {
////        // 1. 기본 프리픽스
////        StringBuilder sb = new StringBuilder("랑쿠재팬 호텔 랭킹 | ");
////
////        // 2. 포인트(지역) 있으면 붙이기
////        if (h.getPointLocation() != null) {
////            sb.append((h.getPointLocation().getTitle())).append(" | ");
////        }
////
////        // 3. 한글 호텔명 (없으면 title 또는 영문명 등 대체)
////        String primaryName = notBlank(h.getKoName()) ? h.getKoName() :
////                notBlank(h.getTitle())  ? h.getTitle()  : "일본 호텔";
////        sb.append(primaryName);
////
////        // 4. 키워드 랜덤 1개
////        String randomKeyword = randomOne(KEYWORD_POOL);
////        sb.append(" | ").append(randomKeyword);
////
////        // 5. 브랜드(사이트명)
////        sb.append(" | 랑쿠재팬, RankuJP");
////
////        // 6. 길이 제어
////        String finalTitle = trimToLength(sb.toString(), TITLE_MAX);
////
////        h.setTitle(finalTitle); // <- 호텔에 title 저장 또는 세팅하는 메서드
////    }
////
////    /** Meta Description 생성 */
////    private void setDescription(Hotel h) {
////        // 호텔 설명을 베이스로 요약 (없으면 기본 카피)
////
////        String finalDesc = trimToLength(h.getDescription(), DESC_MAX);
////
////        h.setDescription(finalDesc);
////    }
////
////    /** Meta Keywords 생성 (선택사항: 요즘 검색엔진 가중치 거의 없음, 내부 분석용 정도) */
////    private void setKeywords(Hotel h) {
////        // 호텔명/지역 + 키워드 풀에서 상위 N개
////        Set<String> set = new LinkedHashSet<>();
////        if (notBlank(h.getKoName())) set.add(h.getKoName());
////        if (h.getPointLocation() != null) set.add(h.getPointLocation().getTitle());
////        set.addAll(pickNRandom(KEYWORD_POOL, 5));
////
////        // 중복 제거된 CSV
////        String csv = set.stream()
////                .map(String::trim)
////                .filter(s -> !s.isBlank())
////                .collect(Collectors.joining(", "));
////
////        h.setKeyword(csv);
////    }
////
////    // ------------------ helpers ------------------
////
////    private static String randomOne(List<String> list) {
////        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
////    }
////
////    private static List<String> pickNRandom(List<String> list, int n) {
////        List<String> copy = new ArrayList<>(list);
////        Collections.shuffle(copy);
////        return copy.subList(0, Math.min(n, copy.size()));
////    }
////
////    private static String trimToLength(String s, int max) {
////        if (s == null) return "";
////        if (s.length() <= max) return s;
////        return s.substring(0, Math.max(0, max - 1)).trim() + "…";
////    }
////
////    private static boolean notBlank(String s) {
////        return s != null && !s.isBlank();
////    }
////
////
////    //---------------pointer ---------------------------
////
////
////    public record LatLng(double lat, double lon) {}
////    @Transactional
////    public void ddd () {
////
////    }
//}
