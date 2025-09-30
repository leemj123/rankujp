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
    public String premium(Model model, @RequestParam(defaultValue = "1") int location, @RequestParam(defaultValue = "1") int type, @RequestParam(defaultValue = "1") int page){
        model.addAttribute("title", "일본 프리미엄 호텔｜일본 5성,4성 호텔 정보, 5성 프리미엄 호텔 1등은?, 1등 할인 보러가기");
        model.addAttribute("thumbnail", "/public/logo");
        model.addAttribute("description", "일본 프리미엄 호텔 추천 & 가격 비교｜예약 사이트 랭킹과 할인 정보 총정리 | 일본 호텔을 가장 합리적으로 예약하려면 언제가 가장 저렴할까? 인기 예약 사이트의 가격・평점・할인 정보까지 한눈에 비교하고, 최저가 예약 꿀팁까지 확인해보세요");
        model.addAttribute("keywords","일본호텔 순위, 호텔 가격 비교, 호텔 할인, 예약사이트, 호텔 예약 꿀팁, 일본호텔추천, 일본호텔순위, 일본호텔랭킹, 일본호텔정보, 일본호텔예약,일본호텔비교");
        model.addAttribute("siteUrl", "");
        model.addAttribute("premiumPage", hotelService.premiumPage(location, type, page));

        if (page == 1)
            model.addAttribute("canonical", "https://rankujp.com/premium");
        else
            model.addAttribute("canonical", "https://rankujp.com/premium?page"+page);

        return "ranku-premium";
    }
}
