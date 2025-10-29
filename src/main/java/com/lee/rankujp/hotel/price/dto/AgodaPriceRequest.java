package com.lee.rankujp.hotel.price.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Getter
public class AgodaPriceRequest {

    private PriceWrapper criteria;

    @Data
    @Builder
    public static class PriceWrapper {
        private final PriceOption additional;
        private final LocalDate checkInDate;
        private final LocalDate checkOutDate;

        private final List<Long> hotelId;

        @Data
        @Builder
        public static class PriceOption {
            private String currency;
            private boolean discountOnly;
            private String language;
            private Occupancy occupancy;
        }
        @Data
        @Builder
        public static class Occupancy {
            private int numberOfAdult;
            private int numberOfChildren;
        }
    }

    public AgodaPriceRequest(LocalDate checkInDate, LocalDate checkOutDate, List<Long> hotelId) {
        this.criteria = PriceWrapper.builder()
                .additional(
                        PriceWrapper.PriceOption.builder()
                        .currency("KRW")
                        .discountOnly(false)
                        .language("ko-kr")
                        .occupancy(
                                PriceWrapper.Occupancy.builder()
                                        .numberOfAdult(2)
                                        .numberOfChildren(0)
                                        .build()
                        )
                        .build()
                )
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .hotelId(hotelId)
                .build();

    }
//    public AgodaPriceRequest(LocalDate checkInDate, LocalDate checkOutDate, long hotelId) {
//        this.criteria = PriceWrapper.builder()
//                .additional(
//                        PriceWrapper.PriceOption.builder()
//                                .currency("JPY")
//                                .discountOnly(false)
//                                .language("ko-kr")
//                                .occupancy(
//                                        PriceWrapper.Occupancy.builder()
//                                                .numberOfAdult(2)
//                                                .numberOfChildren(0)
//                                                .build()
//                                )
//                                .build()
//                )
//                .checkInDate(checkInDate)
//                .checkOutDate(checkOutDate)
//                .hotelId(Collections.singletonList(hotelId))
//                .build();
//
//    }
}
