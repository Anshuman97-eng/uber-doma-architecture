package com.uber.doma.common.kafka;

import com.uber.doma.common.domain.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OutboxEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(OutboxEventPublisher.class);
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(String topic, OutboxEvent event) {
        log.info("[Outbox CDC] Emitting event {} to topic {}", event.getEventType(), topic);
        kafkaTemplate.send(topic, event.getId().toString(), event.getPayload());
        event.markPublished();
    }
}
