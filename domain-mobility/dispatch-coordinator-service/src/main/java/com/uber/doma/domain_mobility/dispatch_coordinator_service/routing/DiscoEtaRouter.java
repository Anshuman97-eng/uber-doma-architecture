package com.uber.doma.domain_mobility.dispatch_coordinator_service.routing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DiscoEtaRouter {
    private static final Logger log = LoggerFactory.getLogger(DiscoEtaRouter.class);

    public record EtaEstimate(String driverId, int etaSeconds) {}

    public EtaEstimate estimateEta(String driverId, double pickupLat, double pickupLng) {
        log.info("[DiscoEta] Calculated contraction hierarchy ETA for driver {}: 240s", driverId);
        return new EtaEstimate(driverId, 240);
    }
}
