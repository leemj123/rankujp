package com.lee.rankujp.big;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class BigController {

    private final BigService bigService;

    @GetMapping("/test/test")
    public void teee() {
        Mono<Void> run  = bigService.secondQueue();
        run.subscribe();
    }
}
