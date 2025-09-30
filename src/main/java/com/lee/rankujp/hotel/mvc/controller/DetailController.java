package com.lee.rankujp.hotel.mvc.controller;

import com.lee.rankujp.hotel.mvc.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class DetailController {

    private final HotelService hotelService;

    @GetMapping("/sale/hotel/{id}")
    public String saleHotel(Model model, @PathVariable Long id, @RequestParam(defaultValue = "0") int top){
        model.addAttribute("top", top);
        model.addAttribute("content", hotelService.HotelDetail(id));
        model.addAttribute("navValue","할인랭킹");
        model.addAttribute("navBack","/");
        return "hotel-detail";
    }

    @GetMapping("/score/hotel/{id}")
    public String scoreHotel(Model model, @PathVariable Long id, @RequestParam(defaultValue = "0") int top){
        model.addAttribute("top", top);
        model.addAttribute("content", hotelService.HotelDetail(id));
        model.addAttribute("navValue","종합랭킹");
        model.addAttribute("navBack","/score");
        return "hotel-detail";
    }

    @GetMapping("/premium/hotel/{id}")
    public String premiumHotel(Model model, @PathVariable Long id, @RequestParam(defaultValue = "0") int top){
        model.addAttribute("top", top);
        model.addAttribute("content", hotelService.HotelDetail(id));
        model.addAttribute("navValue","프리미엄");
        model.addAttribute("navBack","/premium");
        return "hotel-detail";
    }
}
