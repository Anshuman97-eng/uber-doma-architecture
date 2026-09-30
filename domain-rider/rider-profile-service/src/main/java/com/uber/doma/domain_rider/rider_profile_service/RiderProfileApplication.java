package com.uber.doma.domain_rider.rider_profile_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class RiderProfileApplication {
    public static void main(String[] args) {
        SpringApplication.run(RiderProfileApplication.class, args);
    }
}
