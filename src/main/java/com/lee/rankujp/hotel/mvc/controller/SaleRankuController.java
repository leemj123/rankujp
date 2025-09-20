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
@RequestMapping("/")
public class SaleRankuController {

    private final HotelService hotelService;
    @GetMapping
    public String saleRanku(Model model, @RequestParam(defaultValue = "1") int type1, @RequestParam(defaultValue = "1") int type2){
        return "ranku-sale";
    }
}
