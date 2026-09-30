package com.uber.doma.domain_driver.fleet_partner_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class FleetPartnerApplication {
    public static void main(String[] args) {
        SpringApplication.run(FleetPartnerApplication.class, args);
    }
}
