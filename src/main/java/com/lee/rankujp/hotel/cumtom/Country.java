package com.lee.rankujp.hotel.cumtom;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Country {
    JP("JP",3,2);

    private String code;
    private long id;
    private long continentId;
}
