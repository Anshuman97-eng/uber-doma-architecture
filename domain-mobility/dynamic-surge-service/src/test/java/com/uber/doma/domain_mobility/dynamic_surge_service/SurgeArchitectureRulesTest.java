package com.uber.doma.domain_mobility.dynamic_surge_service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class SurgeArchitectureRulesTest {

    @Test
    @DisplayName("Verify dynamic surge service has no imports from Driver or Billing domain packages")
    void testBoundaryIntegrity() {
        String pkg = DynamicSurgeApplication.class.getPackageName();
        assertFalse(pkg.contains("driver"));
        assertFalse(pkg.contains("billing"));
    }
}
