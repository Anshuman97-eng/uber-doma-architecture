package com.uber.doma.domain_rider.auth_security_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthSecurityService {
    private static final Logger log = LoggerFactory.getLogger(AuthSecurityService.class);

    public void processDomainWork() {
        log.info("[AuthSecurity] Executing bounded context domain logic for SSO, MFA & JWT Sessions");
    }
}
