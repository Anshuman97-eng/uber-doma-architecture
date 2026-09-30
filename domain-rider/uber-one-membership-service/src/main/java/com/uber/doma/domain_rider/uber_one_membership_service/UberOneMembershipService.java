package com.uber.doma.domain_rider.uber_one_membership_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UberOneMembershipService {
    private static final Logger log = LoggerFactory.getLogger(UberOneMembershipService.class);

    public void processDomainWork() {
        log.info("[UberOneMembership] Executing bounded context domain logic for Uber One Membership Perks");
    }
}
