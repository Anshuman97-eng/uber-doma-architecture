package com.uber.doma.domain_mobility.dispatch_coordinator_service.reactive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
public class ReactiveCandidateAggregator {
    private static final Logger log = LoggerFactory.getLogger(ReactiveCandidateAggregator.class);

    public record CandidateBatch(List<String> primaryDrivers, List<String> backupDrivers) {}

    public Mono<CandidateBatch> evaluateBatch(String tripId) {
        Mono<List<String>> primaryMono = Mono.just(List.of("d-101", "d-102")).delayElement(Duration.ofMillis(25));
        Mono<List<String>> backupMono = Mono.just(List.of("d-201")).delayElement(Duration.ofMillis(30));

        return Mono.zip(primaryMono, backupMono)
            .map(t -> new CandidateBatch(t.getT1(), t.getT2()))
            .timeout(Duration.ofMillis(100));
    }
}
