package com.lee.rankujp.detected;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/detected")
@RequiredArgsConstructor
public class DetectedController {

    private final DetectedService detectedService;

    @PostMapping("/hotel/detail")
    public void hotelDetailBtnClickDetected(HttpServletRequest request, @RequestBody int detectedId) {
        detectedService.hotelDetected(request, detectedId);
    }
}
