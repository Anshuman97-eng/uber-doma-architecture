package com.uber.doma.domain_mobility.dynamic_surge_service.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SurgeGrpcService {
    private static final Logger log = LoggerFactory.getLogger(SurgeGrpcService.class);

    public record SurgeQuoteResponse(String hexId, double multiplier, boolean active) {}

    public SurgeQuoteResponse getSurge(String hexId) {
        log.info("[SurgeGrpc] Calculated binary gRPC surge quote for hex {}", hexId);
        return new SurgeQuoteResponse(hexId, 1.25, true);
    }
}
