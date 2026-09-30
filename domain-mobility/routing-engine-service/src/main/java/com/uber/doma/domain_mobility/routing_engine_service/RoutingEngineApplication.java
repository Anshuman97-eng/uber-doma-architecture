package com.uber.doma.domain_mobility.routing_engine_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class RoutingEngineApplication {
    public static void main(String[] args) {
        SpringApplication.run(RoutingEngineApplication.class, args);
    }
}
