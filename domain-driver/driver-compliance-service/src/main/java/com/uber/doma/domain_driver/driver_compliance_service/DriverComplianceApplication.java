package com.uber.doma.domain_driver.driver_compliance_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class DriverComplianceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DriverComplianceApplication.class, args);
    }
}
