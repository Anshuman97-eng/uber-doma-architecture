package com.uber.doma.domain_mobility.dynamic_surge_service.routing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SurgeSupplyMigrationRouter {
    private static final Logger log = LoggerFactory.getLogger(SurgeSupplyMigrationRouter.class);

    public record MigrationDetour(String fromHex, String toHex, double detourTimeSec) {}

    public MigrationDetour computeMigrationDetour(String fromHex, String toHex) {
        log.info("[SurgeRouter] Evaluated supply migration detour {} -> {}: 180s", fromHex, toHex);
        return new MigrationDetour(fromHex, toHex, 180.0);
    }
}
