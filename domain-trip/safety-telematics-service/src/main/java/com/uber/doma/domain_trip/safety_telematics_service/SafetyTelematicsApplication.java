package com.uber.doma.domain_trip.safety_telematics_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class SafetyTelematicsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SafetyTelematicsApplication.class, args);
    }
}
