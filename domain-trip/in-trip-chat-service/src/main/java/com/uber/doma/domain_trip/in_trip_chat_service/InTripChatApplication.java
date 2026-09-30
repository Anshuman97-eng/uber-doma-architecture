package com.uber.doma.domain_trip.in_trip_chat_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class InTripChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(InTripChatApplication.class, args);
    }
}
