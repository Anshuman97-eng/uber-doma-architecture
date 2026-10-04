package com.uber.doma.domain_mobility.dispatch_coordinator_service.tracing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DiscoTraceContextPropagator {
    private static final Logger log = LoggerFactory.getLogger(DiscoTraceContextPropagator.class);

    public String buildTraceparent(String traceId, String spanId) {
        String header = String.format("00-%s-%s-01", traceId, spanId);
        log.info("[DiscoTracing] Injected W3C traceparent into dispatch match invocation: {}", header);
        return header;
    }
}
