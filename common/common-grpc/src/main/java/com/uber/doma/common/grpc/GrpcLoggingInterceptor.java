package com.uber.doma.common.grpc;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GrpcLoggingInterceptor implements ServerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(GrpcLoggingInterceptor.class);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        log.info("[gRPC IPC] Invoking Method: {}", call.getMethodDescriptor().getFullMethodName());
        return next.startCall(call, headers);
    }
}
