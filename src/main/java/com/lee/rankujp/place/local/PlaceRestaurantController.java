//package com.lee.rankujp.place.local;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/restaurant")
//public class PlaceRestaurantController {
//    private final PlaceRestaurantService placeRestaurantService;
//
//    @PostMapping("/place/bot")
//    public List<Long> strPlaceBot() {
//       return placeRestaurantService.addRestaurant();
//    }
//
//    @PostMapping("/image")
//    public List<Long> googlePlaceImgGetter() {
//        return placeRestaurantService.fetchAndUpload();
//    }
//}
