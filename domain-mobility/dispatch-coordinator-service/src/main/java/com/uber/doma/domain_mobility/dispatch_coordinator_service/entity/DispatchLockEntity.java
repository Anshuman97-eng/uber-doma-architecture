package com.uber.doma.domain_mobility.dispatch_coordinator_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "dispatch_locks")
public class DispatchLockEntity {

    @Id
    private String tripId;

    private String lockedDriverId;

    @Version
    private Long version;

    public DispatchLockEntity() {}

    public DispatchLockEntity(String tripId, String lockedDriverId) {
        this.tripId = tripId;
        this.lockedDriverId = lockedDriverId;
    }

    public String getTripId() { return tripId; }
    public String getLockedDriverId() { return lockedDriverId; }
    public void setLockedDriverId(String lockedDriverId) { this.lockedDriverId = lockedDriverId; }
    public Long getVersion() { return version; }
}
