package com.uber.doma.domain_billing.driver_instant_payout_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class DriverInstantPayoutApplication {
    public static void main(String[] args) {
        SpringApplication.run(DriverInstantPayoutApplication.class, args);
    }
}
