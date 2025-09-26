package com.lee.rankujp.hotel.mvc.service;

import com.lee.rankujp.hotel.cumtom.PointLocation;
import com.lee.rankujp.hotel.infra.*;
import com.lee.rankujp.hotel.mvc.dto.*;
import com.lee.rankujp.hotel.price.HotelPriceService;
import com.lee.rankujp.hotel.price.dto.AgodaPriceResponse;
import com.lee.rankujp.hotel.price.dto.HotelPriceRow;
import com.lee.rankujp.hotel.repo.HotelRepo;
import com.lee.rankujp.hotel.review.RankuScoreCalculator;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class HotelService {
    private final JPAQueryFactory jpaQueryFactory;
    private final HotelRepo hotelRepo;
    private final QHotel qHotel = QHotel.hotel;
    private final QHotelCity qHotelCity = QHotelCity.hotelCity;
    private final HotelPriceService hotelPriceService;

    //list==================================

    public Page<PremiumResponse> salePage(int location, int sort, int page) {
        Pageable pageable = PageRequest.of(page, 20);
        //
        BooleanExpression predicate = this.filterQueryExpression(location);

        //정렬 null들어가면 에러나서 커버
        List<OrderSpecifier<?>> orders = new ArrayList<>();


        OrderSpecifier<?> order = this.orderType(sort);
        if (order != null) orders.add(order);
        orders.add(qHotel.bestSailPrecent.desc());
        orders.add(qHotel.rankuScore.desc());

        List<Hotel> results = jpaQueryFactory
                .selectFrom(qHotel)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orders.toArray(new OrderSpecifier[0]))
                .fetch();

        long total = Optional.ofNullable(
                jpaQueryFactory
                        .select(qHotel.id.count())
                        .from(qHotel)
                        .where(predicate)
                        .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(results.stream().map(PremiumResponse::new).toList(), pageable, total);
    }
    public Page<ScoreResponse> scorePage(int location, int sort, int page) {
        Pageable pageable = PageRequest.of(page, 20);
        //
        BooleanExpression predicate = this.filterQueryExpression(location);
        //
        List<OrderSpecifier<?>> orders = new ArrayList<>();


        OrderSpecifier<?> order = this.orderType(sort);
        if (order != null) orders.add(order);
        orders.add(qHotel.rankuScore.desc());
        orders.add(qHotel.starRating.desc());

        List<Hotel> results = jpaQueryFactory
                .selectFrom(qHotel)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orders.toArray(new OrderSpecifier[0]))
                .fetch();

        long total = Optional.ofNullable(
                jpaQueryFactory
                        .select(qHotel.id.count())
                        .from(qHotel)
                        .where(predicate)
                        .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(results.stream().map(ScoreResponse::new).toList(), pageable, total);
    }

    public Page<PremiumResponse> premiumPage(int location, int sort, int page) {
        Pageable pageable = PageRequest.of(page, 20);

        BooleanExpression predicate = this.premiumFilterQueryExpression(location);

        List<OrderSpecifier<?>> orders = new ArrayList<>();


        OrderSpecifier<?> order = this.orderType(sort);
        if (order != null) orders.add(order);
        orders.add(qHotel.bestCrossedOutRate.desc());
        orders.add(qHotel.starRating.desc());

        List<Hotel> results = jpaQueryFactory
                .selectFrom(qHotel)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orders.toArray(new OrderSpecifier[0]))
                .fetch();

        long total = Optional.ofNullable(
                jpaQueryFactory
                        .select(qHotel.id.count())
                        .from(qHotel)
                        .where(predicate)
                        .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(results.stream().map(PremiumResponse::new).toList(), pageable, total);

    }

    private  OrderSpecifier<?> orderType (int sort) {
        PathBuilder<Hotel> path = new PathBuilder<>(Hotel.class, "hotel");
        return switch (sort) {
            case 2 -> path.getString("averageFamilyScore").desc();
            case 3 -> path.getString("averageBusinessScore").desc();
            case 4 -> path.getString("averageCoupleScore").desc();
            case 5 -> path.getString("averageSoloScore").desc();
            default -> null;
        };
    }


    private BooleanExpression filterQueryExpression(int location) {
        BooleanExpression predicate = qHotel.bestDailyRate.ne(0.0);

        if (location == 1) {return predicate;}

        if (location < 7) {
            switch (location) {
                case 2: {
                    predicate = predicate.and(qHotel.pointLocation.eq(PointLocation.NAMBA)); break;
                }
                case 3: {
                    predicate = predicate.and(qHotel.pointLocation.eq(PointLocation.UMEDA)); break;
                }
                case 4: {
                    predicate = predicate.and(qHotel.pointLocation.eq(PointLocation.SHINSAIBASHI)); break;
                }
                case 5: {
                    predicate = predicate.and(qHotel.pointLocation.eq(PointLocation.TENOJI)); break;
                }
                case 6: {
                    predicate = predicate.and(qHotel.pointLocation.eq(PointLocation.USJ)); break;
                }
            }
        } else {
            switch (location) {
                case 7: {
                    predicate = predicate.and(qHotel.hotelCity.id.eq(9590L)); break;
                }
                case 8: {
                    predicate = predicate.and(qHotel.hotelCity.id.eq(1784L)); break;
                }
                case 9: {
                    predicate = predicate.and(qHotel.hotelCity.id.eq(5235L)); break;
                }
                case 10: {
                    predicate = predicate.and(qHotel.hotelCity.id.eq(13313L)); break;
                }
            }
        }

        return predicate;
    }
    private BooleanExpression premiumFilterQueryExpression(int location) {
        // 공통 필터 조건
        BooleanExpression common = qHotel.starRating.goe(4.0);

        BooleanExpression predicate;

        if (location == 1) {
            return common; // 바로 리턴
        }

        if (location < 7) {
            predicate = switch (location) {
                case 2 -> qHotel.pointLocation.eq(PointLocation.NAMBA);
                case 3 -> qHotel.pointLocation.eq(PointLocation.UMEDA);
                case 4 -> qHotel.pointLocation.eq(PointLocation.SHINSAIBASHI);
                case 5 -> qHotel.pointLocation.eq(PointLocation.TENOJI);
                case 6 -> qHotel.pointLocation.eq(PointLocation.USJ);
                default -> null; // 미매칭 시 null
            };
        } else if (location < 11) {
            predicate = switch (location) {
                case 7 -> qHotel.hotelCity.id.eq(9590L);
                case 8 -> qHotel.hotelCity.id.eq(1784L);
                case 9 -> qHotel.hotelCity.id.eq(5235L);
                case 10 -> qHotel.hotelCity.id.eq(13313L);
                default -> null;
            };
        } else {
            return common;
        }

        // predicate가 null이면 그냥 common 리턴
        return (predicate != null) ? predicate.and(common) : common;
    }


    //detail================================
    private static final Comparator<HotelPriceResponse> DEAL_DESC =
            Comparator.comparingDouble(HotelPriceResponse::getSailPercent)           // 할인율 큰 순
                    .thenComparingDouble(r -> r.getCrossedOutRate() - r.getDailyRate()) // 절대 할인액 큰 순
                    .thenComparing(HotelPriceResponse::getStayDate)                 // 날짜 늦은 순
                    .reversed(); // 위 세 기준을 전부 반대로(=내림차순) 정렬


    public HotelDetailResponse HotelDetail(Long id) {
        Hotel hotel = hotelRepo.findById(id).orElseThrow();
        HotelCity hotelCity = hotel.getHotelCity();

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
                .keyword(hotel.getKeyword())
                .rankuScore(hotel.getRankuScore())
                .stateName(hotel.getHotelCity().getKoName())
                .stateId(hotel.getHotelCity().getId())
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
                .bestStayDate(hotel.getBestStayDate())
                .bestDailyRate((int)hotel.getBestDailyRate())
                .bestSailPrecent((int)hotel.getBestSailPrecent())
                .bestLink("https://www.agoda.com/ko-kr/search?selectedproperty="+ hotel.getId() +"&checkIn="+ hotel.getBestStayDate() +"&currency=JPY"+
                        "&asq="+hotel.getHotelCity().getAsq())
                .weekdayPriceList(buildTop5(false, hotel, hotelCity))
                .weekendPriceList(buildTop5(true,  hotel, hotelCity))
                .preferenceValue(maxLabel)
                .averageAllScore((int)(hotel.getAverageAllScore() *10))
                .averageBusinessScore((int)(hotel.getAverageBusinessScore()*10))
                .averageCoupleScore((int)(hotel.getAverageCoupleScore()*10))
                .averageSoloScore((int)(hotel.getAverageSoloScore()*10))
                .averageFamilyScore((int)(hotel.getAverageFamilyScore()*10))
                .brandReviewList(hotel.getHotelReviewList().stream().map(HotelReviewResponse::new).toList())
                .build();
    }
    private List<HotelPriceResponse> buildTop5(boolean weekend,
                                               Hotel hotel,
                                               HotelCity hotelCity) {

        Comparator<HotelPriceResponse> DEAL_DESC =
                Comparator.comparingDouble(HotelPriceResponse::getSailPercent)
                        .reversed()
                        .thenComparingDouble(r -> r.getCrossedOutRate() - r.getDailyRate())
                        .reversed()
                        .thenComparing(HotelPriceResponse::getStayDate)
                        .reversed();

        return hotel.getPriceList().stream()
                .filter(Objects::nonNull)
                .filter(p -> !Double.isNaN(p.getSailPercent()))
                .filter(p -> p.getSailPercent() >= 0)
                .filter(p -> p.isWeekend() == weekend)
                .map(p -> new HotelPriceResponse(p, hotel.getId(), hotelCity.getAsq()))
                .sorted(DEAL_DESC)
                .limit(5)
                .toList();
    }
    //other=================================


    @Transactional
    public void scoreCalculator(){
        List<Hotel> hotels = jpaQueryFactory
                .selectFrom(qHotel)
                .fetch();

        hotels.stream().forEach(hotel -> hotel.rankuScoreUpdater(RankuScoreCalculator.hotelScore(hotel)) );
    }

    @Transactional
    public void choiceBeatScore() {
        // N+1 방지를 위해 가능하면 fetch join 쿼리를 미리 준비하세요.
        // 예: hotelRepo.findAllWithPrices()
        List<Hotel> hotels = hotelRepo.findAll();

        for (Hotel h : hotels) {
            List<HotelPrice> prices = h.getPriceList();
            if (prices == null || prices.isEmpty()) {
                // 데이터가 없으면 초기화(원하는 기본값으로 조정 가능)
                h.beatScoreUpdate(null,0d, 0d, 0d);
                continue;
            }

            // 동률일 때 "더 싼 일일가" 우선이 명확하도록 커스텀 비교자 예시:

            HotelPrice best = prices.stream()
                    .filter(Objects::nonNull)
                    .filter(p -> !Double.isNaN(p.getSailPercent())) // primitive double NaN 체크
                    .filter(p -> p.getSailPercent() >= 0)
                    .max((a, b) -> {
                        int byPercent = Double.compare(a.getSailPercent(), b.getSailPercent());
                        if (byPercent != 0) return byPercent;
                        Double ad = a.getDailyRate(), bd = b.getDailyRate();
                        return Double.compare(bd, ad);
                    })
                    .orElse(null);


            if (best != null) {
                h.beatScoreUpdate(best.getStayDate(), nz(best.getCrossedOutRate()), nz(best.getDailyRate()), nz(best.getSailPercent()));

            } else {
                h.beatScoreUpdate(null,0d, 0d, 0d);
            }
        }

        hotelRepo.saveAll(hotels);
    }

    // null-safe double
    private static double nz(Double v) {
        return (v == null || v.isNaN() || v.isInfinite()) ? 0d : v;
    }


    public List<String> getImageList(Long id) {
        Hotel hotel = hotelRepo.findById(id).orElse(null);

        List<String> result = new ArrayList<>();
        result.add(hotel.getThumbnailImg());
        result.add(stripParams(hotel.getPhoto2()));
        result.add(stripParams(hotel.getPhoto3()));
        result.add(stripParams(hotel.getPhoto4()));
        result.add(stripParams(hotel.getPhoto5()));

        return result;
    }
    private String stripParams(String url) {
        if (url == null) return null;
        int idx = url.indexOf("?");
        return (idx >= 0) ? url.substring(0, idx) : url;
    }

    public AgodaPriceResponse.HotelApiInfo getHotelDateSearcher(long id, LocalDate day) {
        AgodaPriceResponse res = hotelPriceService.callApiForDay(day, day.plusDays(2), Collections.singletonList(id)).block();

        if (res == null || res.getResults().isEmpty()) return null;

        return res.getResults().get(0);
    }


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
