package com.lee.rankujp.hotel.mvc.service;

import com.lee.rankujp.hotel.infra.Hotel;
import com.lee.rankujp.hotel.infra.QHotel;
import com.lee.rankujp.hotel.mvc.dto.HotelDetailResponse;
import com.lee.rankujp.hotel.mvc.dto.HotelPriceResponse;
import com.lee.rankujp.hotel.mvc.dto.HotelReviewResponse;
import com.lee.rankujp.hotel.repo.HotelRepo;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelService {
    private final JPAQueryFactory jpaQueryFactory;
    private final HotelRepo hotelRepo;
    private final QHotel qHotel = QHotel.hotel;
    //list==================================


    //detail================================

    public HotelDetailResponse HotelDetail(Long id) {
        Hotel hotel = hotelRepo.findById(id).orElseThrow();

        double max = hotel.getAverageBusinessScore();
        int maxLabel = 1;

        if (hotel.getAverageCoupleScore() > max) {
            max = hotel.getAverageCoupleScore();
            maxLabel = 2;
        }
        if (hotel.getAverageSoloScore() > max) {
            max = hotel.getAverageSoloScore();
            maxLabel = 3;
        }
        if (hotel.getAverageFamilyScore() > max) {
            maxLabel = 4;
        }

        return HotelDetailResponse.builder()
                .id(hotel.getId())
                .updateDate(hotel.getUpdateDateTime().toLocalDate())
                .title(hotel.getTitle())
                .description(hotel.getDescription())
                .koName(hotel.getKoName())
                .address(hotel.getAddress())
                .zipcode(hotel.getZipcode())
                .starRating(hotel.getStarRating())
                .longitude(hotel.getLongitude())
                .latitude(hotel.getLatitude())
                .thumbnailImg(hotel.getThumbnailImg())
                .photo2(hotel.getPhoto2())
                .photo3(hotel.getPhoto3())
                .photo4(hotel.getPhoto4())
                .photo5(hotel.getPhoto5())
                .bestCrossedOutRate((int)hotel.getBestCrossedOutRate())
                .bestDailyRate((int)hotel.getBestDailyRate())
                .bestSailPrecent((int)hotel.getBestSailPrecent())
                .priceList(
                        hotel.getPriceList()
                                .stream()
                                .map(HotelPriceResponse::new)
                                .sorted(
                                        Comparator
                                                .comparing(HotelPriceResponse::getSailPercent)
                                                .reversed()
                                                .thenComparing(HotelPriceResponse::getDailyRate)
                                )
                                .toList()
                )
                .preferenceValue(maxLabel)
                .averageAllScore((int)(hotel.getAverageAllScore() *10))
                .averageBusinessScore((int)(hotel.getAverageBusinessScore()*10))
                .averageCoupleScore((int)(hotel.getAverageCoupleScore()*10))
                .averageSoloScore((int)(hotel.getAverageSoloScore()*10))
                .averageFamilyScore((int)(hotel.getAverageFamilyScore()*10))
                .brandReviewList(hotel.getHotelReviewList().stream().map(HotelReviewResponse::new).toList())
                .build();
    }

    //other=================================

//    @Transactional
//    public void updater() {
//
//        List<Hotel> hotels = jpaQueryFactory
//                .selectFrom(qHotel)
//                .where(qHotel.title.isNull()
//                        .and(qHotel.koName.isNotNull())
//                )
//                .fetch();
//
//        hotels.forEach( (hotel) -> {
//            this.setSEO(hotel);
//        });
//
//    }
//    private static final int TITLE_MAX = 60;
//    private static final int DESC_MAX  = 160;
//
//    private static final List<String> KEYWORD_POOL = List.of(
//            "일본호텔 순위","호텔 가격 비교","호텔 할인","예약사이트","호텔 예약 꿀팁",
//            "일본호텔추천","일본호텔순위","일본호텔랭킹","일본호텔정보","일본호텔예약","일본호텔비교"
//    );
//
//    private void setSEO(Hotel h) {
//        if (h.getDescription() == null || h.getDescription().isBlank()) {
//            this.setDescription(h);
//        }
//        this.setTitle(h);
//        this.setKeywords(h);
//    }
//    /** Title 생성 */
//    private void setTitle(Hotel h) {
//        // 1. 기본 프리픽스
//        StringBuilder sb = new StringBuilder("호텔 추천 랭킹 | ");
//
//        // 2. 포인트(지역) 있으면 붙이기
//        if (h.getPointLocation() != null) {
//            sb.append((h.getPointLocation().getTitle())).append(" | ");
//        }
//
//        // 3. 한글 호텔명 (없으면 title 또는 영문명 등 대체)
//        String primaryName = notBlank(h.getKoName()) ? h.getKoName() :
//                notBlank(h.getTitle())  ? h.getTitle()  : "일본 호텔";
//        sb.append(primaryName);
//
//        // 4. 키워드 랜덤 1개
//        String randomKeyword = randomOne(KEYWORD_POOL);
//        sb.append(" | ").append(randomKeyword);
//
//        // 5. 브랜드(사이트명)
//        sb.append(" | 랭쿠jp, RankuJp");
//
//        // 6. 길이 제어
//        String finalTitle = trimToLength(sb.toString(), TITLE_MAX);
//
//        h.titleUpdater(finalTitle); // <- 호텔에 title 저장 또는 세팅하는 메서드
//    }
//
//    /** Meta Description 생성 */
//    private void setDescription(Hotel h) {
//        // 호텔 설명을 베이스로 요약 (없으면 기본 카피)
//        String base = "일본호텔 랭킹을 통해 가격 비교로 가성비 숙소 쉽게 찾는 법, 할인예약 일본호텔 오사카 숙소 비교부터 할인 예약까지｜후기 기반 호텔 순위, 가격 비교｜최신 일본호텔 정보";
//
//        // 포인트/핵심 속성들 추가
//        List<String> parts = new ArrayList<>();
//        if (h.getPointLocation() != null) {
//            parts.add(h.getPointLocation().getTitle());
//        }
//
//        String tail = parts.isEmpty() ? "" : " · " + String.join(" · ", parts);
//
//        String finalDesc = trimToLength(base.replaceAll("\\s+", " ") + tail, DESC_MAX);
//
//        h.descriptionUpdater(finalDesc); // <- meta description 저장/세팅
//    }
//
//    /** Meta Keywords 생성 (선택사항: 요즘 검색엔진 가중치 거의 없음, 내부 분석용 정도) */
//    private void setKeywords(Hotel h) {
//        // 호텔명/지역 + 키워드 풀에서 상위 N개
//        Set<String> set = new LinkedHashSet<>();
//        if (notBlank(h.getKoName())) set.add(h.getKoName());
//        if (h.getPointLocation() != null) set.add(h.getPointLocation().getTitle());
//        set.addAll(pickNRandom(KEYWORD_POOL, 5));
//
//        // 중복 제거된 CSV
//        String csv = set.stream()
//                .map(String::trim)
//                .filter(s -> !s.isBlank())
//                .collect(Collectors.joining(", "));
//
//        h.keywordUpdater(csv);
//    }
//
//    // ------------------ helpers ------------------
//
//    private static String randomOne(List<String> list) {
//        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
//    }
//
//    private static List<String> pickNRandom(List<String> list, int n) {
//        List<String> copy = new ArrayList<>(list);
//        Collections.shuffle(copy);
//        return copy.subList(0, Math.min(n, copy.size()));
//    }
//
//    private static String trimToLength(String s, int max) {
//        if (s == null) return "";
//        if (s.length() <= max) return s;
//        return s.substring(0, Math.max(0, max - 1)).trim() + "…";
//    }
//
//    private static boolean notBlank(String s) {
//        return s != null && !s.isBlank();
//    }
//    private void setPoint(Hotel h) {
//        PointLocation.from(h.getLatitude(), h.getLongitude())
//                .ifPresent(h::pointUpdater);
//    }
}
