package com.lee.rankujp.hotel.mvc.controller;

import com.lee.rankujp.hotel.mvc.service.HotelService;
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

    @GetMapping("/hotel/{id}")
    public String hotel(Model model, @PathVariable Long id, @RequestParam int top){
        model.addAttribute("top", top);
        model.addAttribute("content", hotelService.HotelDetail(id));
        return "hotel-detail";
    }
}
