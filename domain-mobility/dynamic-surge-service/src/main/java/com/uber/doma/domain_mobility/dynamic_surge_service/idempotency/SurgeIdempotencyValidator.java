package com.uber.doma.domain_mobility.dynamic_surge_service.idempotency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class SurgeIdempotencyValidator {
    private static final Logger log = LoggerFactory.getLogger(SurgeIdempotencyValidator.class);
    private static final String PREFIX = "idemp:surge:";

    private final StringRedisTemplate redisTemplate;

    public SurgeIdempotencyValidator(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean validate(String key) {
        Boolean set = redisTemplate.opsForValue().setIfAbsent(PREFIX + key, "OK", Duration.ofSeconds(30));
        boolean valid = Boolean.TRUE.equals(set);
        if (!valid) {
            log.warn("[SurgeIdemp] Suppressed redundant surge evaluation for key {}", key);
        }
        return valid;
    }
}
