package com.uber.doma.domain_platform.experimentation_flipr_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class ExperimentationFliprApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExperimentationFliprApplication.class, args);
    }
}
