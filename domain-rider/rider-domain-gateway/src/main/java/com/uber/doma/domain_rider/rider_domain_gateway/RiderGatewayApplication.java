package com.uber.doma.domain_rider.rider_domain_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class RiderGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(RiderGatewayApplication.class, args);
    }
}
