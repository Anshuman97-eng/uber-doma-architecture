package com.uber.doma.domain_mobility.dynamic_surge_service.resilience;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SurgeCircuitBreakerConfig {
    private static final Logger log = LoggerFactory.getLogger(SurgeCircuitBreakerConfig.class);

    public double fallbackSurge(String hexId, Throwable t) {
        log.warn("[CircuitBreaker] Surge calculation degraded for hex {}. Falling back to 1.0x baseline: {}", hexId, t.getMessage());
        return 1.0;
    }
}
