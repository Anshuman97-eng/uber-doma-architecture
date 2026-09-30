package com.uber.doma.domain_mobility.dispatch_coordinator_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class DispatchCoordinatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(DispatchCoordinatorApplication.class, args);
    }
}
