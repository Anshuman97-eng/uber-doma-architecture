package com.uber.doma.domain_mobility.dispatch_coordinator_service.resilience;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class DiscoCircuitBreakerConfig {
    private static final Logger log = LoggerFactory.getLogger(DiscoCircuitBreakerConfig.class);

    public List<String> fallbackCandidateList(String tripId, Throwable t) {
        log.warn("[CircuitBreaker] DISCO matching degraded for trip {}: {}", tripId, t.getMessage());
        return Collections.emptyList();
    }
}
