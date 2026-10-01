package com.uber.doma.domain_platform.audit_compliance_service.tracing;

import io.grpc.ServerMethodDefinition;
import io.grpc.stub.ServerCalls;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Test service that captures the trace/span ids observed in MDC while the
 * server handler is running. The latches and atomic references are static so
 * the integration test can assert on them without leaking the channel.
 */
class TestComplianceService implements io.grpc.BindableService {

    private static final Logger log = LoggerFactory.getLogger(TestComplianceService.class);

    static CountDownLatch traceIdLatch = new CountDownLatch(1);
    static CountDownLatch completeLatch = new CountDownLatch(1);
    static final AtomicReference<String> observedTraceId = new AtomicReference<>();
    static final AtomicReference<String> observedSpanId = new AtomicReference<>();

    @Override
    public io.grpc.ServerServiceDefinition bindService() {
        final ServerCalls.UnaryMethod<byte[], byte[]> handler =
                (request, observer) -> handle(request, observer);
        return io.grpc.ServerServiceDefinition.builder("audit.Compliance")
                .addMethod(ServerMethodDefinition.create(TestDescriptors.METHOD,
                        ServerCalls.asyncUnaryCall(handler)))
                .build();
    }

    private void handle(final byte[] request,
                        final StreamObserver<byte[]> observer) {
        observedTraceId.set(MDC.get(TracingConstants.MDC_TRACE_ID));
        observedSpanId.set(MDC.get(TracingConstants.MDC_SPAN_ID));
        traceIdLatch.countDown();
        log.info("[TEST SERVER] observed traceId={} spanId={}",
                observedTraceId.get(), observedSpanId.get());
        final String payload = request == null ? "" : new String(request, StandardCharsets.UTF_8);
        if (payload.contains("error")) {
            completeLatch.countDown();
            observer.onError(io.grpc.Status.INTERNAL.withDescription("synthetic failure").asRuntimeException());
            return;
        }
        final byte[] body = payload.contains("hello")
                ? "ack:hello".getBytes(StandardCharsets.UTF_8)
                : "ack".getBytes(StandardCharsets.UTF_8);
        observer.onNext(body);
        observer.onCompleted();
        completeLatch.countDown();
    }
}