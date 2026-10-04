package com.uber.doma.domain_mobility.dispatch_coordinator_service.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class DiscoMetricsManager {

    private final Timer matchDurationTimer;

    public DiscoMetricsManager(MeterRegistry registry) {
        this.matchDurationTimer = Timer.builder("uber.disco.match.duration")
            .description("Time taken to complete algorithmic dispatch matching round")
            .publishPercentiles(0.50, 0.95, 0.99)
            .minimumExpectedValue(Duration.ofMillis(5))
            .maximumExpectedValue(Duration.ofMillis(1000))
            .register(registry);
    }

    public Timer getMatchDurationTimer() {
        return matchDurationTimer;
    }
}
