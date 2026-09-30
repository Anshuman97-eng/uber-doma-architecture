package com.uber.doma.domain_driver.driver_earnings_analytics_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class DriverEarningsAnalyticsApplication {
    public static void main(String[] args) {
        SpringApplication.run(DriverEarningsAnalyticsApplication.class, args);
    }
}
