package com.uber.doma.domain_rider.fraud_gps_spoofing_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FraudGpsSpoofingService {
    private static final Logger log = LoggerFactory.getLogger(FraudGpsSpoofingService.class);

    public void processDomainWork() {
        log.info("[FraudGpsSpoofing] Executing bounded context domain logic for Anti-Fraud & GPS Spoofing Detection");
    }
}
