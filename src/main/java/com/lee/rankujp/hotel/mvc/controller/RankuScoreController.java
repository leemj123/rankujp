package com.lee.rankujp.hotel.mvc.controller;

import com.lee.rankujp.hotel.mvc.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class RankuScoreController {

    private final HotelService hotelService;

    @GetMapping("/score")
    public String rankuScore(Model model, @RequestParam(defaultValue = "1") int location, @RequestParam(defaultValue = "1") int type, @RequestParam(defaultValue = "1") int page ){
        model.addAttribute("title", "랑쿠재팬 일본 호텔 평점 랭킹 | 오사카, 교토, 고배, 나라의 모든 호텔 종합");
        model.addAttribute("thumbnail", "/public/logo");
        model.addAttribute("description", "일본의 호텔들을 모든 리뷰와 가격정보들을 종합해 랑쿠재팬만의 점수로 호텔랭킹으로 새롭게 소개합니다.");
        model.addAttribute("keywords","일본호텔 순위, 호텔 가격 비교, 호텔 할인, 예약사이트, 호텔 예약 꿀팁, 일본호텔추천, 일본호텔순위, 일본호텔랭킹, 일본호텔정보, 일본호텔예약,일본호텔비교");
        model.addAttribute("scorePage", hotelService.scorePage(location, type, page, null));

        if (page == 1)
            model.addAttribute("canonical", "https://rankujp.com/score");
        else
            model.addAttribute("canonical", "https://rankujp.com/score?page"+page);
        return "ranku-score";
    }
    @GetMapping("/kyushu/score")
    public String kyushuScore(Model model, @RequestParam(defaultValue = "1") int location, @RequestParam(defaultValue = "1") int area, @RequestParam(defaultValue = "1") int type, @RequestParam(defaultValue = "1") int page ){
        model.addAttribute("title", "랑쿠재팬 규슈 호텔 종합 랭킹 | 후쿠오카,유후인,벳푸,사가,나가사키");
        model.addAttribute("thumbnail", "/public/logo");
        model.addAttribute("description", "랑쿠재팬에서 규슈의 모든 호텔 종합랭킹을 한눈에. 온천 여행이 가고싶을 때는 랑쿠재팬을 통해서 후쿠오카,유후인,벳푸,사가,아리타등 호텔 총집합");
        model.addAttribute("keywords","규슈호텔평점, 규슈 호텔, 온천 여행, 후쿠오카,유후인,벳푸,구마모토,사가,우레시노,아리타 ");
        model.addAttribute("scorePage", hotelService.kyushuScorePage(location, area, type, page, null));

        if (page == 1)
            model.addAttribute("canonical", "https://rankujp.com/kyushu/score");
        else
            model.addAttribute("canonical", "https://rankujp.com/kyushu/score?page"+page);
        return "kyushu-score";
    }
}
