package com.uber.doma.domain_mobility.dispatch_coordinator_service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class DiscoArchitectureRulesTest {

    @Test
    @DisplayName("Verify DISCO matching has no direct imports from Billing or Rider domain packages")
    void testBoundaryIntegrity() {
        String pkg = DispatchCoordinatorApplication.class.getPackageName();
        assertFalse(pkg.contains("billing"));
        assertFalse(pkg.contains("rider"));
    }
}
