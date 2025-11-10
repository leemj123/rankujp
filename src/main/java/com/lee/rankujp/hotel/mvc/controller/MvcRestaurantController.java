package com.lee.rankujp.hotel.mvc.controller;

import com.lee.rankujp.place.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class MvcRestaurantController {

    private final RestaurantService restaurantService;
    
    @GetMapping("/restaurant")
    public String restaurantPage(Model model, @RequestParam(defaultValue = "1") int location, @RequestParam(defaultValue = "1") int type, @RequestParam(defaultValue = "1") int page){
        model.addAttribute("title", "랑쿠재팬 - 일본맛집랭킹 | 오사카, 고배, 교토, 나라의 맛집을 전부 모아 리뷰와 평점으로 한눈에");
        model.addAttribute("thumbnail", "/public/logo");
        model.addAttribute("description", "구글 리뷰와 평점을 바탕으로 맛집 데이터 총 집합! 랑쿠재팬에서 지역별로 모아 일본맛집랭킹을 한눈에");
        model.addAttribute("keywords","일본맛집 순위, 오사카 맛집, 고배맛집, 교토맛집, 나라맛집");
        model.addAttribute("restaurantPage", restaurantService.restaurantPage(location, type, page));

        if (page == 1)
            model.addAttribute("canonical", "https://rankujp.com/restaurant");
        else
            model.addAttribute("canonical", "https://rankujp.com/restaurant/?page"+page);

        return "restaurant";
    }
    @GetMapping("/kyushu/restaurant")
    public String kyushuRestaurantPage(Model model, @RequestParam(defaultValue = "1") int location, @RequestParam(defaultValue = "1") int area, @RequestParam(defaultValue = "1") int type, @RequestParam(defaultValue = "1") int page){
        model.addAttribute("title", "랑쿠재팬 - 규슈맛집랭킹 | 후쿠오카,유후인,벳푸,사가등 맛집을 전부 모아 리뷰와 평점으로 한눈에");
        model.addAttribute("thumbnail", "/public/logo");
        model.addAttribute("description", "규슈지역 전체 맛집을 모아 구글 리뷰와 평점을 바탕으로 데이터 랭킹! 랑쿠재팬에서 규슈의 지역별로 모아 규슈맛집 랭킹을 한눈에");
        model.addAttribute("keywords","규슈맛집 순위, 후쿠오카 맛집, 유후인 맛집, 벳푸 맛집, 사가 맛집, 아리타 맛집");
        model.addAttribute("restaurantPage", restaurantService.kyushuRestaurantPage(location, area, type, page));

        if (page == 1)
            model.addAttribute("canonical", "https://rankujp.com/kyushu/restaurant");
        else
            model.addAttribute("canonical", "https://rankujp.com/kyushu/restaurant?page"+page);

        return "kyushu-restaurant";
    }
}
