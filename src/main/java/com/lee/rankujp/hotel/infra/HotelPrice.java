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
public class HotelPrice {
    @EmbeddedId
    private HotelPriceId id;

    private double crossedOutRate;

    private double dailyRate;

    private double salePercent;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean isWeekend;


    /* 편의 접근자 */
    public LocalDate getStayDate() { return id.getStayDate(); }

}
