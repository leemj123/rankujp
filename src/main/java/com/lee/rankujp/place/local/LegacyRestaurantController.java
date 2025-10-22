//package com.lee.rankujp.place.local;
//
//
//import com.lee.rankujp.place.RestaurantService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/restaurant")
//public class LegacyRestaurantController {
//    private final RestaurantService restaurantService;
//
//    @PostMapping("/{id}")
//    public List<Long> strPlaceBot(@PathVariable Long id) {
//       return restaurantService.addRestaurant(id);
//    }
//
//    @PostMapping("/image")
//    public List<Long> googlePlaceImgGetter() {
//        return restaurantService.fetchAndUpload();
//    }
//}
