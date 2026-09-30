package com.uber.doma.domain_billing.fare_quotation_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FareQuotationService {
    private static final Logger log = LoggerFactory.getLogger(FareQuotationService.class);

    public void processDomainWork() {
        log.info("[FareQuotation] Executing bounded context domain logic for Upfront Guaranteed Fare Estimation");
    }
}
