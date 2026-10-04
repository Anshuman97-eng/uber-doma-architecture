package com.uber.doma.domain_mobility.dynamic_surge_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "surge_rule_configs")
public class SurgeRuleConfigEntity {

    @Id
    private String ruleId;

    private Double maxSurgeCap;

    @Version
    private Long version;

    public SurgeRuleConfigEntity() {}

    public SurgeRuleConfigEntity(String ruleId, Double maxSurgeCap) {
        this.ruleId = ruleId;
        this.maxSurgeCap = maxSurgeCap;
    }

    public String getRuleId() { return ruleId; }
    public Double getMaxSurgeCap() { return maxSurgeCap; }
    public void setMaxSurgeCap(Double maxSurgeCap) { this.maxSurgeCap = maxSurgeCap; }
    public Long getVersion() { return version; }
}
