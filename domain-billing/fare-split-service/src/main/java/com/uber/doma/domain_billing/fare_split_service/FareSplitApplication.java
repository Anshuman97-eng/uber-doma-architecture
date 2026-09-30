package com.uber.doma.domain_billing.fare_split_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class FareSplitApplication {
    public static void main(String[] args) {
        SpringApplication.run(FareSplitApplication.class, args);
    }
}
