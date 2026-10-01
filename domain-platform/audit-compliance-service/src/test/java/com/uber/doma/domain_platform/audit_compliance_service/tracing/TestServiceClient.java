package com.uber.doma.domain_platform.audit_compliance_service.tracing;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.stub.ClientCalls;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Test-only thin client around {@link ClientCalls} that returns a
 * {@link Future} so the integration tests can assert on completion.
 */
final class TestServiceClient {

    private final Channel channel;
    private final ClientInterceptor extraInterceptor;

    TestServiceClient(final Channel channel, final ClientInterceptor extraInterceptor) {
        this.channel = channel;
        this.extraInterceptor = extraInterceptor;
    }

    Future<String> unary(final String payload) {
        final Channel effective = extraInterceptor == null
                ? channel
                : io.grpc.ClientInterceptors.intercept(channel, extraInterceptor);
        final CallOptions options = CallOptions.DEFAULT;
        final ClientCall<byte[], byte[]> call = effective.newCall(TestDescriptors.METHOD, options);
        final CompletableFuture<String> future = new CompletableFuture<>();
        ClientCalls.asyncUnaryCall(call,
                payload.getBytes(StandardCharsets.UTF_8),
                new io.grpc.stub.StreamObserver<byte[]>() {
                    @Override
                    public void onNext(final byte[] value) {
                        future.complete(new String(value, StandardCharsets.UTF_8));
                    }

                    @Override
                    public void onError(final Throwable throwable) {
                        future.completeExceptionally(throwable);
                    }

                    @Override
                    public void onCompleted() {
                        // Completion handled in onNext for this test.
                    }
                });
        return future;
    }
}