package com.uber.doma.domain_billing.fare_split_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FareSplitService {
    private static final Logger log = LoggerFactory.getLogger(FareSplitService.class);

    public void processDomainWork() {
        log.info("[FareSplit] Executing bounded context domain logic for Multi-Rider Fare Splitter");
    }
}
