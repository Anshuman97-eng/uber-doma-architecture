package com.uber.doma.domain_rider.uber_one_membership_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class UberOneMembershipApplication {
    public static void main(String[] args) {
        SpringApplication.run(UberOneMembershipApplication.class, args);
    }
}
