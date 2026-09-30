package com.uber.doma.domain_billing.driver_instant_payout_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DriverInstantPayoutService {
    private static final Logger log = LoggerFactory.getLogger(DriverInstantPayoutService.class);

    public void processDomainWork() {
        log.info("[DriverInstantPayout] Executing bounded context domain logic for Instant Pay Disbursement");
    }
}
