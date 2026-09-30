package com.uber.doma.domain_platform.distributed_rate_limiter_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class DistributedRateLimiterApplication {
    public static void main(String[] args) {
        SpringApplication.run(DistributedRateLimiterApplication.class, args);
    }
}
