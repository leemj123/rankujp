package com.lee.rankujp.hotel.mvc;

import com.lee.rankujp.hotel.mvc.dto.PremiumResponse;
import com.lee.rankujp.hotel.mvc.dto.ScoreResponse;
import com.lee.rankujp.hotel.mvc.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rest")
public class RestFulController {
    private final HotelService hotelService;

    @GetMapping("/sale")
    public Page<PremiumResponse> sale(@RequestParam(defaultValue = "1") int location, @RequestParam(defaultValue = "1") int type, @RequestParam(defaultValue = "1") int page){
        return hotelService.salePage(location, type, page);
    }
    @GetMapping("/score")
    public Page<ScoreResponse> score(@RequestParam(defaultValue = "1") int location, @RequestParam(defaultValue = "1") int type, @RequestParam(defaultValue = "1") int page){
        return hotelService.scorePage(location, type, page);
    }
    @GetMapping("/premium")
    public Page<PremiumResponse> premium(@RequestParam(defaultValue = "1") int location, @RequestParam(defaultValue = "1") int type, @RequestParam(defaultValue = "1") int page){
        return hotelService.premiumPage(location, type, page);
    }
    @GetMapping("/file/hotel/{id}")
    public List<String> hotelPhoto(@PathVariable Long id){
        return hotelService.getImageList(id);
    }
}
