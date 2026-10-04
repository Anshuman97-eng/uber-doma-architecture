package com.uber.doma.domain_mobility.dynamic_surge_service.tracing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SurgeTracePropagator {
    private static final Logger log = LoggerFactory.getLogger(SurgeTracePropagator.class);

    public String buildTraceparent(String traceId, String spanId) {
        String header = String.format("00-%s-%s-01", traceId, spanId);
        log.info("[SurgeTracing] Injected W3C traceparent into surge quote: {}", header);
        return header;
    }
}
