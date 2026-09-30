package com.uber.doma.mobility.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Service
public class MobilityScatterGatherOrchestrator {
    private static final Logger log = LoggerFactory.getLogger(MobilityScatterGatherOrchestrator.class);

    public Mono<String> orchestrateRideOffer(String riderId, String pickupH3, String dropoffH3) {
        log.info("[Tier-2 Gateway] Scatter-Gather dispatch for rider {}", riderId);
        Mono<String> supplyMono = Mono.just("DriverFound:drv_102").delayElement(Duration.ofMillis(25));
        Mono<String> surgeMono = Mono.just("Surge:1.25x").delayElement(Duration.ofMillis(15));
        Mono<String> etaMono = Mono.just("ETA:4min").delayElement(Duration.ofMillis(30));

        return Mono.zip(supplyMono, surgeMono, etaMono)
                   .map(tuple -> String.format("CompositeOffer{%s, %s, %s}", tuple.getT1(), tuple.getT2(), tuple.getT3()));
    }
}
