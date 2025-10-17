package com.lee.rankujp.place;

import com.lee.rankujp.place.dto.RestaurantResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/restaurant")
public class RestaurantController {
    private final RestaurantService restaurantService;

    @GetMapping("/location")
    public List<RestaurantResponseDto>  getRestaurantLocation(@RequestParam double lat, @RequestParam double lon) {
        return restaurantService.getRestaurantLocation(lat, lon);
    }
}
