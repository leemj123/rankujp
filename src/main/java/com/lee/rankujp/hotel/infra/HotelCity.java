package com.lee.rankujp.hotel.infra;

import com.lee.rankujp.place.infra.Restaurant;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

@Entity
@Getter
public class HotelCity {
    @Id
    private Long id;

    @OneToMany(mappedBy = "hotelCity")
    private List<Hotel> hotelList;

    @OneToMany(mappedBy = "hotelCity")
    private List<Restaurant> restaurantList;
    private String state;
    private String koName;
    @Lob
    private String asq;

}
