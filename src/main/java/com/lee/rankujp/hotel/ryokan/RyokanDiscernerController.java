//package com.lee.rankujp.hotel.ryokan;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/odongdev")
//public class RyokanDiscernerController {
//
//    private final RyokanDiscernerService discernerService;
//
//    @PostMapping("/ryokan")
//    public long goRyokan() {
//        return discernerService.ryokanDiscerner();
//    }
//    @PostMapping("/list")
//    public void goSave(@RequestBody List<PostmanSave> ids) {
//        discernerService.goSave(ids);
//    }
//}
