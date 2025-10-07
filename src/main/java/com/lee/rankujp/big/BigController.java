package com.lee.rankujp.big;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BigController {

    private final BigService bigService;

    @PostMapping("/big/ja")
    public List<JaNameDto> test(@RequestBody List<Long> ids) {
        return bigService.hello(ids);
    }
}
