package com.uber.doma.domain_mobility.dynamic_surge_service.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class SurgeMetricsManager {

    private final Timer surgeCalculationTimer;

    public SurgeMetricsManager(MeterRegistry registry) {
        this.surgeCalculationTimer = Timer.builder("uber.surge.calc.duration")
            .description("Time taken to evaluate dynamic surge multiplier")
            .publishPercentiles(0.50, 0.95, 0.99)
            .minimumExpectedValue(Duration.ofMillis(1))
            .maximumExpectedValue(Duration.ofMillis(200))
            .register(registry);
    }

    public Timer getSurgeCalculationTimer() {
        return surgeCalculationTimer;
    }
}
