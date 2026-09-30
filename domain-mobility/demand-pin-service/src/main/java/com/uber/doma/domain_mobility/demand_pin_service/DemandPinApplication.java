package com.uber.doma.domain_mobility.demand_pin_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class DemandPinApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemandPinApplication.class, args);
    }
}
