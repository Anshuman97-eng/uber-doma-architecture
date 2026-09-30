package com.uber.doma.domain_driver.driver_onboarding_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class DriverOnboardingApplication {
    public static void main(String[] args) {
        SpringApplication.run(DriverOnboardingApplication.class, args);
    }
}
