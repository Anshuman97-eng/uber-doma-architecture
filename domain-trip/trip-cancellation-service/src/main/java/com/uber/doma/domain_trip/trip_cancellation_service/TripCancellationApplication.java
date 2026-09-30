package com.uber.doma.domain_trip.trip_cancellation_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class TripCancellationApplication {
    public static void main(String[] args) {
        SpringApplication.run(TripCancellationApplication.class, args);
    }
}
