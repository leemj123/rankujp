package com.lee.rankujp.hotel.mvc.controller;

import com.lee.rankujp.hotel.mvc.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class SaleRankuController {

    private final HotelService hotelService;

    @GetMapping
    public String sale(Model model, @RequestParam(defaultValue = "1") int location, @RequestParam(defaultValue = "1") int type, @RequestParam(defaultValue = "1") int page){
        model.addAttribute("title", "랑쿠재팬 - 일본 호텔 할인 랭킹 | 오사카,교토,고배,나라 호텔 가격・평점 비교로 저렴하게 호텔 찾는 법");
        model.addAttribute("thumbnail", "/public/logo");
        model.addAttribute("description", "일본의 모든 호텔을 모아 한눈에 가격 비교 - 랑쿠재팬이 준비한 할인 정보 총정리 | 일본 호텔을 가장 합리적으로 예약하려면 랑쿠재팬만의 실시간 갱신 호텔 가성비");
        model.addAttribute("keywords","일본호텔 순위, 호텔 가격 비교, 호텔 할인, 랑쿠재팬, 예약사이트, 호텔 예약 꿀팁, 일본호텔추천, 일본호텔순위, 일본호텔랭킹, 일본호텔정보, 일본호텔예약, 일본호텔비교");
        model.addAttribute("salePage", hotelService.salePage(location, type, page, null, false));

        if (page == 1)
            model.addAttribute("canonical", "https://rankujp.com");
        else
            model.addAttribute("canonical", "https://rankujp.com/?page"+page);

        return "ranku-sale";
    }
    @GetMapping("/kyushu")
    public String kyushuSale(Model model, @RequestParam(defaultValue = "1") int location, @RequestParam(defaultValue = "1") int area, @RequestParam(defaultValue = "1") int type, @RequestParam(defaultValue = "1") int page){
        model.addAttribute("title", "랑쿠재팬 - 규슈 할인 랭킹 | 후쿠오카,유후인,벳푸,사가 지금 가장 할인 높은 호텔");
        model.addAttribute("thumbnail", "/public/logo");
        model.addAttribute("description", "랑쿠재팬이 준비한 규슈 전 지역 할인 정보 총정리 | 온천을 가장 합리적으로 예약하려면 언제가 가장 저렴한지 한눈에 비교");
        model.addAttribute("keywords","일본호텔 순위, 호텔 가격 비교, 호텔 할인, 랑쿠재팬, 예약사이트, 일본호텔랭킹, 일본호텔정보, 일본호텔예약, 일본호텔비교");
        model.addAttribute("salePage", hotelService.kyushuSalePage(location, area, type, page, null, false));

        if (page == 1)
            model.addAttribute("canonical", "https://rankujp.com/kyushu");
        else
            model.addAttribute("canonical", "https://rankujp.com/kyushu?page"+page);

        return "kyushu-sale";
    }

    @GetMapping("/ranku-terms-of-service")
    public String termsOfService(Model model){
        model.addAttribute("title", "이용약관 - 랑쿠재팬 | RankuJP");
        model.addAttribute("thumbnail", "/public/logo");
        model.addAttribute("description", "랑쿠재팬을 이용해주셔셔 감사합니다. 본 사이트의 할인율 및 가격 정보는 각 예약사이트에서 제공받은 데이터를 기반으로 합니다. 최종 예약 및 결제는 해당 예약사이트에서 진행되며, 가격 변동이 있을 수 있습니다.");
        model.addAttribute("keywords","랑쿠재팬,이용약관,공식정보,랭크, 할인, 할인율");

        return "terms-of-use";
    }
}