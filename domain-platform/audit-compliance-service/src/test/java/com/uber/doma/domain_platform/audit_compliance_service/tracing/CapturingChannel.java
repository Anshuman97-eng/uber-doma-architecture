package com.uber.doma.domain_platform.audit_compliance_service.tracing;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Test-only {@link Channel} wrapper that records the {@code traceparent}
 * header present in outgoing call headers.
 */
final class CapturingChannel extends Channel {

    private final Channel delegate;
    private final AtomicReference<String> observed;

    CapturingChannel(final Channel delegate, final AtomicReference<String> observed) {
        this.delegate = delegate;
        this.observed = observed;
    }

    @Override
    public <RequestT, ResponseT> ClientCall<RequestT, ResponseT> newCall(
            final MethodDescriptor<RequestT, ResponseT> methodDescriptor,
            final CallOptions callOptions) {
        final ClientCall<RequestT, ResponseT> call = delegate.newCall(methodDescriptor, callOptions);
        return new ForwardingClientCall.SimpleForwardingClientCall<RequestT, ResponseT>(call) {
            @Override
            public void start(final Listener<ResponseT> responseListener, final Metadata headers) {
                observed.set(headers.get(Metadata.Key.of("traceparent", Metadata.ASCII_STRING_MARSHALLER)));
                super.start(responseListener, headers);
            }
        };
    }

    @Override
    public String authority() {
        return delegate.authority();
    }
}