package com.uber.doma.domain_driver.vehicle_registry_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class VehicleRegistryService {
    private static final Logger log = LoggerFactory.getLogger(VehicleRegistryService.class);

    public void processDomainWork() {
        log.info("[VehicleRegistry] Executing bounded context domain logic for Vehicle Registry & Class Mapping");
    }
}
