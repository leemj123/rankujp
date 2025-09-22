package com.lee.rankujp.hotel.price.function;

import com.lee.rankujp.hotel.price.dto.HotelPriceRow;
import com.lee.rankujp.hotel.price.dto.TopBucket;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

public final class Top5ChoiceHotel {
    private static final int K = 5; // 평일 5, 주말 5

    // 최소힙 comparator (오름차순: '덜 좋은' 게 peek)
    private final Comparator<HotelPriceRow> asc =
            Comparator.comparingDouble(HotelPriceRow::getSailPercent)
                    .thenComparingDouble(r -> r.getCrossedOutRate() - r.getDailyRate())
                    .thenComparing(HotelPriceRow::getStayDate);

    private final Map<Long, PQPair> map = new HashMap<>();

    private static boolean isWeekend(LocalDate d) {
        var dow = d.getDayOfWeek();
        return (dow == java.time.DayOfWeek.SATURDAY || dow == java.time.DayOfWeek.SUNDAY);
    }

    private final class PQPair {
        final PriorityQueue<HotelPriceRow> weekdays = new PriorityQueue<>(K, asc);
        final PriorityQueue<HotelPriceRow> weekends = new PriorityQueue<>(K, asc);
    }

    /** (hotelId, row) 한 건 수집 */
    public void accept(long hotelId, HotelPriceRow row) {
        var p = map.computeIfAbsent(hotelId, id -> new PQPair());
        PriorityQueue<HotelPriceRow> pq = isWeekend(row.getStayDate()) ? p.weekends : p.weekdays;

        if (pq.size() < K) pq.add(row);
        else if (asc.compare(pq.peek(), row) < 0) { pq.poll(); pq.add(row); }
    }

    /** 부분 결과 병합 (병렬 수집 대비) */
    public void combine(Top5ChoiceHotel other) {
        other.map.forEach((hid, o) -> {
            var p = map.computeIfAbsent(hid, id -> new PQPair());
            mergeInto(p.weekdays, o.weekdays);
            mergeInto(p.weekends, o.weekends);
        });
    }

    private void mergeInto(PriorityQueue<HotelPriceRow> dst, PriorityQueue<HotelPriceRow> src) {
        for (var row : src) {
            if (dst.size() < K) dst.add(row);
            else if (asc.compare(dst.peek(), row) < 0) { dst.poll(); dst.add(row); }
        }
    }

    /** 최종 결과 (각 버킷 내림차순 정렬, 최대 5개 유지) */
    public Map<Long, TopBucket> finish() {
        var desc = asc.reversed();
        return map.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> new TopBucket(
                        e.getValue().weekdays.stream().sorted(desc).limit(K).toList(),
                        e.getValue().weekends.stream().sorted(desc).limit(K).toList()
                )
        ));
    }
}
