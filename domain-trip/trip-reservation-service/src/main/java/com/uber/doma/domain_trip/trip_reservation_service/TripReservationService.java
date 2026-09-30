package com.uber.doma.domain_trip.trip_reservation_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TripReservationService {
    private static final Logger log = LoggerFactory.getLogger(TripReservationService.class);

    public void processDomainWork() {
        log.info("[TripReservation] Executing bounded context domain logic for Uber Reserve Scheduled Trips");
    }
}
