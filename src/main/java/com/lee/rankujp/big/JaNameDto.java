package com.lee.rankujp.big;

import lombok.Getter;

@Getter
public class JaNameDto {
    private final long id;
    private final String jaName;

    public JaNameDto(AgodaJa agodaJa) {
        this.id = agodaJa.getC1();
        this.jaName = agodaJa.getC8();
    }
    public JaNameDto(long id, String name) {
        this.id = id;
        this.jaName = name;
    }
}
