package com.lee.rankujp.hotel.price;
import com.lee.rankujp.hotel.infra.Hotel;
import com.lee.rankujp.hotel.price.dto.HotelPriceRow;
import com.lee.rankujp.hotel.price.dto.ImgStarResponse;
import com.lee.rankujp.hotel.price.dto.TopBucket;
import com.lee.rankujp.hotel.repo.HotelRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Stream;

import static com.lee.rankujp.hotel.price.HotelPriceService.DB_ELASTIC;


@Service
@RequiredArgsConstructor
@Slf4j
public class HotelPersistService {
    private final HotelRepo hotelRepo;
    private final DataSource dataSource;
    private final TransactionTemplate tx;

    private final String INSERT_SQL = "INSERT INTO hotel_price (hotel_id, stay_date, crossed_out_rate, daily_rate, sail_percent, is_weekend, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static double safe(Double v) {
        return (v == null || !Double.isFinite(v)) ? 0d : v;
    }

    public Mono<Void> truncateHotelPrice() {
        return Mono.fromRunnable(() -> {
            try (Connection c = dataSource.getConnection();
                 Statement s = c.createStatement()) {
                s.execute("TRUNCATE TABLE hotel_price");
                // 필요하면 AUTO_INCREMENT 명시 초기화
                // s.execute("ALTER TABLE hotel_price AUTO_INCREMENT = 1");
            } catch (SQLException e) {
                throw new RuntimeException("TRUNCATE hotel_price failed", e);
            }
        }).subscribeOn(DB_ELASTIC).then();
    }
    public Mono<Void> reloadHotelPrices(Map<Long, TopBucket> topMap) {

        if (topMap == null || topMap.isEmpty()) return Mono.empty();

        return Mono.fromRunnable(() -> {

            // 2) 대량 INSERT (트랜잭션으로 묶음)
            tx.executeWithoutResult(st -> {
                final int FLUSH = 2000; // 배치 플러시 크기
                int batch = 0;
                final Timestamp nowTs = Timestamp.valueOf(LocalDateTime.now());

                Connection conn = DataSourceUtils.getConnection(dataSource);
                try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
                    for (var e : topMap.entrySet()) { // 정렬 불필요 시 그대로 순회
                        Long hid = e.getKey();
                        TopBucket bucket = e.getValue();

                        for (HotelPriceRow row : iterate(bucket)) {
                            ps.setLong(1, hid);
                            ps.setDate(2, Date.valueOf(row.getStayDate()));
                            ps.setDouble(3, safe(row.getCrossedOutRate()));
                            ps.setDouble(4, safe(row.getDailyRate()));
                            ps.setDouble(5, safe(row.getSailPercent()));
                            ps.setByte(6, (byte) (row.isWeekend() ? 1 : 0));
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
                    throw new RuntimeException("HotelPrice reload (insert batch) failed", ex);
                } finally {
                    DataSourceUtils.releaseConnection(conn, dataSource);
                }
            });

        }).subscribeOn(Schedulers.boundedElastic()).then();
    }


    /** TopBucket 내부의 두 리스트(평/주) 순회 */
    private static Iterable<HotelPriceRow> iterate(TopBucket b) {
        return () -> Stream.concat(b.weekdayList().stream(), b.weekendList().stream()).iterator();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveImgStarInNewTx(long hotelId, ImgStarResponse resp) {
        Hotel h = hotelRepo.findById(hotelId).orElseThrow();
        h.imgStarUpdate(resp.getResults().get(0));
        hotelRepo.saveAndFlush(h);
    }
}

