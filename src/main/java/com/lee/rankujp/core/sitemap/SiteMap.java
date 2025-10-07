package com.lee.rankujp.core.sitemap;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class SiteMap {
    private final long id;
    private final LocalDate modifiedTime;

    public SiteMap (Long id, LocalDateTime time) {
        this.id = id;
        this.modifiedTime = time.toLocalDate();
    }
}
