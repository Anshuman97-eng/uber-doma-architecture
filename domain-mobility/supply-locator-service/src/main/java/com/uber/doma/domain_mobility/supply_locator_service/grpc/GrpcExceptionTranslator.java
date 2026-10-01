package com.uber.doma.domain_mobility.supply_locator_service.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GrpcExceptionTranslator {
    private static final Logger log = LoggerFactory.getLogger(GrpcExceptionTranslator.class);

    public StatusRuntimeException translate(Throwable t) {
        if (t instanceof IllegalArgumentException) {
            log.warn("[GrpcException] Bad request argument: {}", t.getMessage());
            return Status.INVALID_ARGUMENT.withDescription(t.getMessage()).asRuntimeException();
        }

        log.error("[GrpcException] Internal server error: {}", t.getMessage(), t);
        return Status.INTERNAL.withDescription("Internal error in supply locator").asRuntimeException();
    }
}
