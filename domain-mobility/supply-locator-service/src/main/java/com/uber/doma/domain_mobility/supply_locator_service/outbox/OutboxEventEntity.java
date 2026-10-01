package com.uber.doma.domain_mobility.supply_locator_service.outbox;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "outbox_events")
public class OutboxEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private String aggregateId;

    @Column(nullable = false, length = 4096)
    private String payloadJson;

    @Column(nullable = false)
    private String status; // PENDING, PUBLISHED, FAILED

    @Column(nullable = false)
    private Instant createdAt;

    public OutboxEventEntity() {}

    public OutboxEventEntity(String aggregateType, String aggregateId, String payloadJson) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.payloadJson = payloadJson;
        this.status = "PENDING";
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getAggregateType() { return aggregateType; }
    public String getAggregateId() { return aggregateId; }
    public String getPayloadJson() { return payloadJson; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
}
