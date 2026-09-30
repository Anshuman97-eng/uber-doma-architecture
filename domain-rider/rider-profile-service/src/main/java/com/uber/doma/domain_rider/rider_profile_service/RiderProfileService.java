package com.uber.doma.domain_rider.rider_profile_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RiderProfileService {
    private static final Logger log = LoggerFactory.getLogger(RiderProfileService.class);

    public void processDomainWork() {
        log.info("[RiderProfile] Executing bounded context domain logic for Rider Profile & Preferences");
    }
}
