package com.uber.doma.domain_mobility.demand_pin_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DemandPinService {
    private static final Logger log = LoggerFactory.getLogger(DemandPinService.class);

    public void processDomainWork() {
        log.info("[DemandPin] Executing bounded context domain logic for Rider Demand & Pin Drop Intake");
    }
}
