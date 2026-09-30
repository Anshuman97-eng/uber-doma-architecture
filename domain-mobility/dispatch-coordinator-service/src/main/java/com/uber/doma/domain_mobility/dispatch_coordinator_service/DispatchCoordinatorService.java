package com.uber.doma.domain_mobility.dispatch_coordinator_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DispatchCoordinatorService {
    private static final Logger log = LoggerFactory.getLogger(DispatchCoordinatorService.class);

    public void processDomainWork() {
        log.info("[DispatchCoordinator] Executing bounded context domain logic for DISCO Algorithmic Matcher");
    }
}
