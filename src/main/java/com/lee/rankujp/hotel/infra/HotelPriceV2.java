package com.lee.rankujp.hotel.infra;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Builder @NoArgsConstructor @AllArgsConstructor
@Getter
public class HotelPriceV2 {
    @EmbeddedId
    private HotelPriceId id;

    @MapsId("hotelId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @Column(nullable = false)
    private double crossedOutRate;

    @Column(nullable = false)
    private double dailyRate;

    @Column(nullable = false)
    private double sailPercent;

    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean isWeekend;

    /* 편의 접근자 */
    public LocalDate getStayDate() { return id.getStayDate(); }

}
