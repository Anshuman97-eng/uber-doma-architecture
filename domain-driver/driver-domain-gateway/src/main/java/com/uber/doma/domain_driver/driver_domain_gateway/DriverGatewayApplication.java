package com.uber.doma.domain_driver.driver_domain_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class DriverGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(DriverGatewayApplication.class, args);
    }
}
