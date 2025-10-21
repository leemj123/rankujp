package com.lee.rankujp.place.infra;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@NoArgsConstructor
public class TestRestaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String formattedAddress;
    //Location
    private double latitude;
    private double longitude;

    //displayName
    private String title;

    //PrimaryTypeDisplayName
    private String primaryText;
    private String primaryType;

    private String separateAdders1;
    private String separateAdders2;
    private String separateAdders3;
    private String separateAdders4;
    private String separateAdders5;
    private String separateAdders6;

}
