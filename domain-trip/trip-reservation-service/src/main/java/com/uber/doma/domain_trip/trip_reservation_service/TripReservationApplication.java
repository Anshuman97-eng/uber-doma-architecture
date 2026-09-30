package com.uber.doma.domain_trip.trip_reservation_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class TripReservationApplication {
    public static void main(String[] args) {
        SpringApplication.run(TripReservationApplication.class, args);
    }
}
