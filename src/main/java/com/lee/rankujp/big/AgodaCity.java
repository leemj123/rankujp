package com.lee.rankujp.big;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgodaCity {
    @Id
    @Column(name = "city_id")
    private long cityId;
    private String cityName;
    private String countryCode;
    private long count;
}
