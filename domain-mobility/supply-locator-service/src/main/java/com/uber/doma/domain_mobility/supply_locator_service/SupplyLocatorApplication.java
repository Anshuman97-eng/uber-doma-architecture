package com.uber.doma.domain_mobility.supply_locator_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class SupplyLocatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(SupplyLocatorApplication.class, args);
    }
}
