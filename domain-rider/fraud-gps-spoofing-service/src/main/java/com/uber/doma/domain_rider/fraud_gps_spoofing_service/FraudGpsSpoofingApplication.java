package com.uber.doma.domain_rider.fraud_gps_spoofing_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class FraudGpsSpoofingApplication {
    public static void main(String[] args) {
        SpringApplication.run(FraudGpsSpoofingApplication.class, args);
    }
}
