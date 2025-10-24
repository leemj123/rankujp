package com.lee.rankujp.hotel.infra;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HotelPriceId implements Serializable {
    private Long hotelId;
    private LocalDate stayDate;
}
