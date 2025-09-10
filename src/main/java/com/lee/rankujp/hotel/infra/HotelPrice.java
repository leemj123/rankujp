package com.lee.rankujp.hotel.infra;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_hotel_price",
                        columnNames = {"hotel_id", "stay_date"}
                )
        },
        indexes = {
                @Index(name = "idx_price_hotel_date", columnList = "hotel_id, stay_date")
        }
)
public class HotelPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "price_label_id")
    private PriceLabel priceLabel;

    @Column(nullable = false)
    private LocalDate stayDate;

    @Column(nullable = false)
    private double crossedOutRate;

    @Column(nullable = false)
    private double dailyRate;

    @Column(name = "sail_percent", nullable = false, precision = 5, scale = 2)
    private double sailPercent;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
