package com.uber.doma.domain_mobility.pool_batching_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class PoolBatchingApplication {
    public static void main(String[] args) {
        SpringApplication.run(PoolBatchingApplication.class, args);
    }
}
