package com.lee.rankujp.hotel.infra;

import com.lee.rankujp.hotel.cumtom.ReviewBrand;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HotelReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Enumerated(EnumType.STRING)
    private ReviewBrand reviewBrand;

    private long reviewCount;
    private double allScore;

}
