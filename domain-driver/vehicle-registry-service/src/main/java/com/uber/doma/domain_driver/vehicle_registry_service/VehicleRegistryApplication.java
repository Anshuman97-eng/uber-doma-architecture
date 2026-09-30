package com.uber.doma.domain_driver.vehicle_registry_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class VehicleRegistryApplication {
    public static void main(String[] args) {
        SpringApplication.run(VehicleRegistryApplication.class, args);
    }
}
