package com.uber.doma.domain_trip.trip_state_machine_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class TripStateMachineApplication {
    public static void main(String[] args) {
        SpringApplication.run(TripStateMachineApplication.class, args);
    }
}
