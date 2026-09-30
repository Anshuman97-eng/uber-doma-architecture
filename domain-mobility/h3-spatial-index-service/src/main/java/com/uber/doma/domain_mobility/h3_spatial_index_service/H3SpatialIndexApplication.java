package com.uber.doma.domain_mobility.h3_spatial_index_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class H3SpatialIndexApplication {
    public static void main(String[] args) {
        SpringApplication.run(H3SpatialIndexApplication.class, args);
    }
}
