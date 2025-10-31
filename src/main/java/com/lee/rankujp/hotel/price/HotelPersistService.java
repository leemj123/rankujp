package com.lee.rankujp.hotel.price;

import com.lee.rankujp.hotel.infra.HotelPrice;
import com.lee.rankujp.hotel.price.dto.HotelPriceRow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class HotelPersistService {

    private final DataSource dataSource;
    private final TransactionTemplate tx;

    private final String UPSERT_SQL =
            "INSERT INTO hotel_price ( hotel_id, stay_date, crossed_out_rate, daily_rate, sale_percent, is_weekend, updated_at)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE" +
                    " crossed_out_rate = VALUES(crossed_out_rate)," +
                    " daily_rate = VALUES(daily_rate)," +
                    " sale_percent = VALUES(sale_percent)," +
                    " is_weekend = VALUES(is_weekend)," +
                    " updated_at = VALUES(updated_at)";

    private final String BEST_UPSERT_SQL = """
                INSERT INTO hotel (
                       id, best_stay_date, best_crossed_out_rate, best_daily_rate, best_sale_precent, update_date_time,
                       average_group_score, average_couple_score, latitude, longitude,
                       average_business_score, star_rating, average_solo_score, ranku_score,
                       average_family_score, average_all_score, is_show, review_num, prefecture_code
                     )
                     SELECT ?, ?, ?, ?, ?, ?,
                            average_group_score, average_couple_score, latitude, longitude,
                            average_business_score, star_rating, average_solo_score, ranku_score,
                            average_family_score, average_all_score, is_show, review_num, prefecture_code
                     FROM hotel WHERE id = ?
                     ON DUPLICATE KEY UPDATE
                       best_stay_date        = VALUES(best_stay_date),
                       best_crossed_out_rate = VALUES(best_crossed_out_rate),
                       best_daily_rate       = VALUES(best_daily_rate),
                       best_sale_precent     = VALUES(best_sale_precent),
                       update_date_time      = VALUES(update_date_time);
            """;

    private static double safe(Double v) {
        return (v == null || !Double.isFinite(v)) ? 0d : v;
    }

    public Mono<Void> reloadHotelPrices(List<HotelPriceRow> apiData) {

        if (apiData == null || apiData.isEmpty()) return Mono.empty();

        return Mono.fromRunnable(() -> {

            // 2) 대량 INSERT (트랜잭션으로 묶음)
            tx.executeWithoutResult(st -> {
                final int FLUSH = 2000; // 배치 플러시 크기
                int batch = 0;
                final Timestamp nowTs = Timestamp.valueOf(LocalDateTime.now());

                Connection conn = DataSourceUtils.getConnection(dataSource);
                try (PreparedStatement ps = conn.prepareStatement(UPSERT_SQL)) {

                    for (HotelPriceRow row : apiData) {

                        ps.setLong(1, row.getHotelId());
                        ps.setDate(2, Date.valueOf(row.getStayDate()));
                        ps.setDouble(3, safe(row.getCrossedOutRate()));
                        ps.setDouble(4, safe(row.getDailyRate()));
                        ps.setDouble(5, safe(row.getSalePercent()));
                        ps.setByte(6, (byte) (row.isWeekend() ? 1 : 0));
                        ps.setTimestamp(7, nowTs);

                        ps.addBatch();
                        if (++batch == FLUSH) {
                            ps.executeBatch();
                            ps.clearBatch();
                            batch = 0;
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

    //============================================================

    public Mono<Void> reviseHotelBestPrice(List<HotelPrice> bestPriceList) {

        return Mono.fromRunnable(() -> {

            // 2) 대량 (트랜잭션으로 묶음)
            tx.executeWithoutResult(st -> {
                final int FLUSH = 2000; // 배치 플러시 크기
                int batch = 0;
                final Timestamp nowTs = Timestamp.valueOf(LocalDateTime.now());

                Connection conn = DataSourceUtils.getConnection(dataSource);
                try (PreparedStatement ps = conn.prepareStatement(BEST_UPSERT_SQL)) {

                    for (HotelPrice p : bestPriceList) {

                        if (p.getDailyRate() == 0.0) {
                            ps.setLong(1, p.getId().getHotelId());
                            ps.setObject(2, null);
                            ps.setObject(3, 0.0);
                            ps.setObject(4,  0.0);
                            ps.setObject(5, 0.0);
                            ps.setObject(6, nowTs);
                            ps.setLong(7, p.getId().getHotelId());
                        } else {
                            ps.setLong(1, p.getId().getHotelId());
                            ps.setObject(2, p.getId().getStayDate());
                            ps.setObject(3, p.getCrossedOutRate());
                            ps.setObject(4,  p.getDailyRate());
                            ps.setObject(5, p.getSalePercent());
                            ps.setObject(6, nowTs);
                            ps.setLong(7, p.getId().getHotelId());
                        }
                        ps.addBatch();
                        if (++batch == FLUSH) {
                            ps.executeBatch();
                            ps.clearBatch();
                            batch = 0;
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
}

