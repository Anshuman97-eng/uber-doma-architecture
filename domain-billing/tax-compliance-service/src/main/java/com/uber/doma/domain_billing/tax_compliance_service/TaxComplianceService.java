package com.uber.doma.domain_billing.tax_compliance_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TaxComplianceService {
    private static final Logger log = LoggerFactory.getLogger(TaxComplianceService.class);

    public void processDomainWork() {
        log.info("[TaxCompliance] Executing bounded context domain logic for Rideshare Tax & Surcharges");
    }
}
