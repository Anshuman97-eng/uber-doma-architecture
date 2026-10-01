package com.uber.doma.domain_mobility.supply_locator_service.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "driver_location_history", indexes = {
    @Index(name = "idx_driver_timestamp", columnList = "driverId, recordedAt")
})
public class DriverLocationHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String driverId;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Instant recordedAt;

    public DriverLocationHistoryEntity() {}

    public DriverLocationHistoryEntity(String driverId, Double latitude, Double longitude, Instant recordedAt) {
        this.driverId = driverId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.recordedAt = recordedAt;
    }

    public Long getId() { return id; }
    public String getDriverId() { return driverId; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public Instant getRecordedAt() { return recordedAt; }
}
