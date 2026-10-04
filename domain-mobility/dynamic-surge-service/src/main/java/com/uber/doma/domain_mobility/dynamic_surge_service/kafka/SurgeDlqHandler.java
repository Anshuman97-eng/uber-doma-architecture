package com.uber.doma.domain_mobility.dynamic_surge_service.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SurgeDlqHandler {
    private static final Logger log = LoggerFactory.getLogger(SurgeDlqHandler.class);
    public static final String SURGE_DLQ_TOPIC = "mobility.dynamic-surge.dlq";

    public void handlePoisonPill(String payload, Exception e) {
        log.error("[SurgeDLQ] Offloaded corrupt message to {}: {}", SURGE_DLQ_TOPIC, e.getMessage());
    }
}
