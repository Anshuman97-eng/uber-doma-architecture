package com.uber.doma.domain_rider.auth_security_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class AuthSecurityApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthSecurityApplication.class, args);
    }
}
