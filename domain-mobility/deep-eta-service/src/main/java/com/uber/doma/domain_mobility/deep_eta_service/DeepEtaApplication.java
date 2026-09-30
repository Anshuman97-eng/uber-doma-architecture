package com.uber.doma.domain_mobility.deep_eta_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class DeepEtaApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeepEtaApplication.class, args);
    }
}
