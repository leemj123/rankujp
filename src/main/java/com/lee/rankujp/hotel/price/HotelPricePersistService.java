package com.lee.rankujp.hotel.price;

import com.lee.rankujp.hotel.price.dto.HotelPriceRow;
import com.lee.rankujp.hotel.price.dto.TopBucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@RequiredArgsConstructor
@Slf4j
public class HotelPricePersistService {

    private final DataSource dataSource;
    private final TransactionTemplate tx;

    // 데드락 감지용(간단 예시)
    public boolean isDeadlockLike(Throwable t) {
        String msg = String.valueOf(t.getMessage());
        return msg.contains("Deadlock") || msg.contains("1213") || msg.contains("40001");
    }

    private static double safe(Double v) {
        return (v == null || !Double.isFinite(v)) ? 0d : v;
    }

    public Mono<Void> upsertTopBucketMap(Map<Long, TopBucket> topMap) {
        if (topMap == null || topMap.isEmpty()) return Mono.empty();

        final String SQL = """
        INSERT INTO hotel_price
          (hotel_id, stay_date, crossed_out_rate, daily_rate, sail_percent, is_weekend, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
          crossed_out_rate = VALUES(crossed_out_rate),
          daily_rate       = VALUES(daily_rate),
          sail_percent     = VALUES(sail_percent),
          is_weekend       = VALUES(is_weekend),
          updated_at       = VALUES(updated_at)
        """;

        return Mono.fromRunnable(() ->
                tx.executeWithoutResult(st -> {
                    final int FLUSH = 2000;
                    int batch = 0;
                    final Timestamp nowTs = Timestamp.valueOf(LocalDateTime.now());

                    // ✅ 트랜잭션 연동 커넥션
                    Connection conn = DataSourceUtils.getConnection(dataSource);
                    try (PreparedStatement ps = conn.prepareStatement(SQL)) {
                        for (var e : new TreeMap<>(topMap).entrySet()) {
                            Long hid = e.getKey();
                            TopBucket b = e.getValue();

                            for (HotelPriceRow row : iterate(b)) {
                                ps.setLong(1, hid);
                                ps.setDate(2, Date.valueOf(row.getStayDate()));
                                ps.setDouble(3, safe(row.getCrossedOutRate()));
                                ps.setDouble(4, safe(row.getDailyRate()));
                                ps.setDouble(5, safe(row.getSailPercent()));
                                ps.setByte(6, (byte) (row.isWeekend() ? 1 : 0));  // TINYINT(1)
                                ps.setTimestamp(7, nowTs);

                                ps.addBatch();
                                if (++batch == FLUSH) {
                                    ps.executeBatch();
                                    ps.clearBatch();
                                    batch = 0;
                                }
                            }
                        }
                        if (batch > 0) {
                            ps.executeBatch();
                            ps.clearBatch();
                        }
                    } catch (SQLException ex) {
                        throw new RuntimeException("HotelPrice upsertTopBucketMap failed", ex);
                    } finally {
                        DataSourceUtils.releaseConnection(conn, dataSource);
                    }
                })
        ).subscribeOn(Schedulers.boundedElastic()).then();
    }
    public Mono<Void> deletePastBefore(LocalDate baseDate, Set<Long> hotelIds) {
        if (hotelIds == null || hotelIds.isEmpty()) return Mono.empty();

        return Mono.fromRunnable(() -> {
            String placeholders = hotelIds.stream().map(id -> "?").collect(Collectors.joining(","));
            String sql = """
                DELETE FROM hotel_price
                 WHERE stay_date < ?
                   AND hotel_id IN (%s)
            """.formatted(placeholders);

            try (var conn = dataSource.getConnection();
                 var ps = conn.prepareStatement(sql)) {
                int idx = 1;
                ps.setObject(idx++, baseDate);
                for (Long id : hotelIds) ps.setLong(idx++, id);
                ps.executeUpdate();
            } catch (Exception ex) {
                throw new RuntimeException("HotelPrice deletePastBefore failed", ex);
            }
        });
    }

    /** TopBucket 내부의 두 리스트(평/주) 순회 */
    private static Iterable<HotelPriceRow> iterate(TopBucket b) {
        return () -> Stream.concat(b.weekdayList().stream(), b.weekendList().stream()).iterator();
    }
}
