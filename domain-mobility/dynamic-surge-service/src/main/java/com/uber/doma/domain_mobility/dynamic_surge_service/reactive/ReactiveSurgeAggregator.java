package com.uber.doma.domain_mobility.dynamic_surge_service.reactive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class ReactiveSurgeAggregator {
    private static final Logger log = LoggerFactory.getLogger(ReactiveSurgeAggregator.class);

    public record SurgeFactors(int localDemand, int surroundingSupply) {}

    public Mono<SurgeFactors> aggregateFactors(String hexId) {
        Mono<Integer> demandMono = Mono.just(85).delayElement(Duration.ofMillis(15));
        Mono<Integer> supplyMono = Mono.just(40).delayElement(Duration.ofMillis(20));

        return Mono.zip(demandMono, supplyMono)
            .map(t -> new SurgeFactors(t.getT1(), t.getT2()))
            .timeout(Duration.ofMillis(80));
    }
}
