package com.uber.doma.domain_mobility.dynamic_surge_service.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class SurgeMultiplierCache {
    private static final Logger log = LoggerFactory.getLogger(SurgeMultiplierCache.class);
    private static final String SURGE_KEY = "mobility:surge:multiplier:";

    private final StringRedisTemplate redisTemplate;

    public SurgeMultiplierCache(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void cacheMultiplier(String hexId, double multiplier) {
        redisTemplate.opsForValue().set(SURGE_KEY + hexId, String.valueOf(multiplier), Duration.ofSeconds(15));
        log.info("[SurgeCache] Cached multiplier {}x for hex {} (TTL 15s)", multiplier, hexId);
    }
}
