package com.lee.rankujp.hotel.repo;
import com.lee.rankujp.hotel.infra.Hotel;
import com.lee.rankujp.place.infra.PlaceImg;
import org.springframework.data.jpa.repository.JpaRepository;
public interface HotelRepo extends JpaRepository<Hotel, Long>{
}
