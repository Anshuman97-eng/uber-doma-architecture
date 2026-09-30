package com.uber.doma.domain_rider.rider_domain_gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RiderGatewayService {
    private static final Logger log = LoggerFactory.getLogger(RiderGatewayService.class);

    public void processDomainWork() {
        log.info("[RiderGateway] Executing bounded context domain logic for Tier 2 Rider Gateway");
    }
}
