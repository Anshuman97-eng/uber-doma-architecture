package com.uber.doma.domain_trip.toll_highway_surcharge_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TollHighwaySurchargeService {
    private static final Logger log = LoggerFactory.getLogger(TollHighwaySurchargeService.class);

    public void processDomainWork() {
        log.info("[TollHighwaySurcharge] Executing bounded context domain logic for Electronic Toll Detection");
    }
}
