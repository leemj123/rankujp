package com.lee.rankujp.hotel.mvc.controller;

import com.lee.rankujp.hotel.mvc.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/premium")
public class PremiumController {

    private final HotelService hotelService;

    @GetMapping
    public String premium(Model model, @RequestParam(defaultValue = "1") int location, @RequestParam(defaultValue = "1") int type){
        return "ranku-premium";
    }
}
