package com.uber.doma.domain_trip.trip_state_machine_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TripStateMachineService {
    private static final Logger log = LoggerFactory.getLogger(TripStateMachineService.class);

    public void processDomainWork() {
        log.info("[TripStateMachine] Executing bounded context domain logic for Trip State Machine Coordinator");
    }
}
