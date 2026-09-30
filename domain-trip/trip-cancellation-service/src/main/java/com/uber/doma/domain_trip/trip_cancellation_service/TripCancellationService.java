package com.uber.doma.domain_trip.trip_cancellation_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TripCancellationService {
    private static final Logger log = LoggerFactory.getLogger(TripCancellationService.class);

    public void processDomainWork() {
        log.info("[TripCancellation] Executing bounded context domain logic for Trip Cancellation & Refund Logic");
    }
}
