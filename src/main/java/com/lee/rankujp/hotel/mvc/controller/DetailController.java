package com.lee.rankujp.hotel.mvc.controller;

import com.lee.rankujp.hotel.mvc.dto.HotelDetailResponse;
import com.lee.rankujp.hotel.mvc.service.CookieControl;
import com.lee.rankujp.hotel.mvc.service.HotelService;
import com.lee.rankujp.place.RestaurantService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class DetailController {

    private final HotelService hotelService;
    private final RestaurantService restaurantService;


    @GetMapping("/hotel/{id}")
    public String saleHotel(Model model, HttpServletRequest req, HttpServletResponse res
            , @PathVariable Long id, @RequestParam(defaultValue = "0") int top){

        HotelDetailResponse h = hotelService.HotelDetail(id);

        CookieControl.upsertCookie("https://rankujp.com/hotel/"+h.getId()+"?top="+top ,h , req, res);

        model.addAttribute("top", top);
        model.addAttribute("content", h);
        model.addAttribute("navValue","할인랭킹");
        model.addAttribute("navBack","https://rankujp.com");
        model.addAttribute("isVariant", false);
        model.addAttribute("restaurant", restaurantService.getRestaurantLocation(h.getLatitude(), h.getLongitude()));

        return "hotel-detail";
    }

    @GetMapping("/score/hotel/{id}")
    public String scoreHotel(Model model, HttpServletRequest req, HttpServletResponse res
            , @PathVariable Long id, @RequestParam(defaultValue = "0") int top){

        HotelDetailResponse h = hotelService.HotelDetail(id);

        CookieControl.upsertCookie("https://rankujp.com/score/hotel/"+h.getId()+"?top="+top ,h , req, res);

        model.addAttribute("top", top);
        model.addAttribute("content", h);
        model.addAttribute("navValue","종합랭킹");
        model.addAttribute("navBack","https://rankujp.com/score");
        model.addAttribute("isVariant", true);
        model.addAttribute("restaurant", restaurantService.getRestaurantLocation(h.getLatitude(), h.getLongitude()));
        return "hotel-detail";
    }

    @GetMapping("/premium/hotel/{id}")
    public String premiumHotel(Model model, HttpServletRequest req, HttpServletResponse res
            , @PathVariable Long id, @RequestParam(defaultValue = "0") int top){

        HotelDetailResponse h = hotelService.HotelDetail(id);

        CookieControl.upsertCookie("https://rankujp.com/premium/hotel/"+h.getId()+"?top="+top ,h , req, res);

        model.addAttribute("top", top);
        model.addAttribute("content", h);
        model.addAttribute("navValue","프리미엄");
        model.addAttribute("navBack","https://rankujp.com/premium");
        model.addAttribute("isVariant", true);
        model.addAttribute("restaurant", restaurantService.getRestaurantLocation(h.getLatitude(), h.getLongitude()));
        return "hotel-detail";
    }


}
