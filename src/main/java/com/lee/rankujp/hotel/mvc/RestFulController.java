package com.lee.rankujp.hotel.mvc;

import com.lee.rankujp.hotel.mvc.dto.HotelWithPrice;
import com.lee.rankujp.hotel.mvc.dto.HotelWithScore;
import com.lee.rankujp.hotel.mvc.dto.PremiumResponse;
import com.lee.rankujp.hotel.mvc.service.HotelService;
import com.lee.rankujp.hotel.price.dto.AgodaPriceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rest")
public class RestFulController {
    private final HotelService hotelService;

    @GetMapping("/sale")
    public Page<HotelWithPrice> sale(@RequestParam(defaultValue = "1") int location, @RequestParam(defaultValue = "1") int type,
                                     @RequestParam(defaultValue = "1") int page, @RequestParam(required = false)LocalDate searchDate,
                                     @RequestParam(defaultValue = "false") boolean price){
        return hotelService.salePage(location, type, page, searchDate, price);
    }
    @GetMapping("/score")
    public Page<HotelWithScore> score(@RequestParam(defaultValue = "1") int location, @RequestParam(defaultValue = "1") int type,
                                      @RequestParam(defaultValue = "1") int page, @RequestParam(required = false)LocalDate searchDate){
        return hotelService.scorePage(location, type, page, searchDate);
    }
    @GetMapping("/premium")
    public Page<PremiumResponse> premium(@RequestParam(defaultValue = "1") int location, @RequestParam(defaultValue = "1") int type, @RequestParam(defaultValue = "1") int page){
        return hotelService.premiumPage(location, type, page);
    }
    @GetMapping("/file/hotel/{id}")
    public List<String> hotelPhoto(@PathVariable Long id){
        return hotelService.getImageList(id);
    }
    @GetMapping("/search/hotel/{id}/date")
    public AgodaPriceResponse.HotelApiInfo hotelDateSearcher(@PathVariable Long id , @RequestParam LocalDate day) {
        return hotelService.getHotelDateSearcher(id, day);
    }

}
