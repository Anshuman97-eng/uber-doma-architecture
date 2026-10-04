package com.uber.doma.domain_mobility.dispatch_coordinator_service.idempotency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class DiscoIdempotencyValidator {
    private static final Logger log = LoggerFactory.getLogger(DiscoIdempotencyValidator.class);
    private static final String PREFIX = "idemp:disco:";

    private final StringRedisTemplate redisTemplate;

    public DiscoIdempotencyValidator(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean validateMatchRequest(String idempotencyKey) {
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(PREFIX + idempotencyKey, "MATCHING", Duration.ofSeconds(60));
        boolean valid = Boolean.TRUE.equals(acquired);
        if (!valid) {
            log.warn("[DiscoIdemp] Suppressed duplicate match submission for key: {}", idempotencyKey);
        }
        return valid;
    }
}
