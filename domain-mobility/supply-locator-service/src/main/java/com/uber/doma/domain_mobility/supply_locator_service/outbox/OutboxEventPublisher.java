package com.uber.doma.domain_mobility.supply_locator_service.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(OutboxEventPublisher.class);

    @Transactional
    public void stageEvent(String aggregateType, String aggregateId, String payloadJson) {
        OutboxEventEntity event = new OutboxEventEntity(aggregateType, aggregateId, payloadJson);
        log.info("[Outbox] Staged atomic outbox event for {} (ID: {})", aggregateType, aggregateId);
    }
}
