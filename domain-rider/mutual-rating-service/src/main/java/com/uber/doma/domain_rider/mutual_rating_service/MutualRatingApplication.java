package com.uber.doma.domain_rider.mutual_rating_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class MutualRatingApplication {
    public static void main(String[] args) {
        SpringApplication.run(MutualRatingApplication.class, args);
    }
}
