package com.lee.rankujp.hotel.infra;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;

import java.util.List;

@Entity
@Getter
public class HotelCity {
    @Id
    private Long id;

    @OneToMany(mappedBy = "hotelCity")
    private List<Hotel> hotelList;

    private String state;
    private String koName;

}
