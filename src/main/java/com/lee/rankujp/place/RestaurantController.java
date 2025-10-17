package com.lee.rankujp.place;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/restaurant")
public class RestaurantController {
    private final RestaurantService restaurantService;

    @PostMapping("/{id}")
    public List<Long> strPlaceBot(@PathVariable Long id) {
       return restaurantService.addRestaurant(id);
    }

    @PostMapping("/image")
    public List<Long> googlePlaceImgGetter() {
        return restaurantService.fetchAndUpload();
    }

    @DeleteMapping()
    public void fd() {
        restaurantService.testst();
    }
}
