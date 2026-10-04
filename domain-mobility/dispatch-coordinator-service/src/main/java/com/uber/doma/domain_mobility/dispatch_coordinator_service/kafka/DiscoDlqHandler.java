package com.uber.doma.domain_mobility.dispatch_coordinator_service.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DiscoDlqHandler {
    private static final Logger log = LoggerFactory.getLogger(DiscoDlqHandler.class);
    public static final String DISCO_DLQ_TOPIC = "mobility.dispatch-coordinator.dlq";

    public void routeFailedMatch(String tripId, String reason) {
        log.warn("[DiscoDLQ] Routing failed match for trip {} to {}: {}", tripId, DISCO_DLQ_TOPIC, reason);
    }
}
