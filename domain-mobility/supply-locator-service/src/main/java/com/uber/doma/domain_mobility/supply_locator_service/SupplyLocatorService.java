package com.uber.doma.domain_mobility.supply_locator_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SupplyLocatorService {
    private static final Logger log = LoggerFactory.getLogger(SupplyLocatorService.class);

    public void processDomainWork() {
        log.info("[SupplyLocator] Executing bounded context domain logic for Driver Supply & Geolocation");
    }
}
