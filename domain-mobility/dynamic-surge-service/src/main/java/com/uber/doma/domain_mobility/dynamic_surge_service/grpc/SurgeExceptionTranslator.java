package com.uber.doma.domain_mobility.dynamic_surge_service.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.stereotype.Component;

@Component
public class SurgeExceptionTranslator {

    public StatusRuntimeException translate(Throwable t) {
        if (t instanceof IllegalArgumentException) {
            return Status.INVALID_ARGUMENT.withDescription(t.getMessage()).asRuntimeException();
        }
        return Status.INTERNAL.withDescription("Internal error in dynamic surge service").asRuntimeException();
    }
}
