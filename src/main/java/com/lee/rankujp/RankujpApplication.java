package com.lee.rankujp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RankujpApplication {

    public static void main(String[] args) {
        SpringApplication.run(RankujpApplication.class, args);
    }

}
