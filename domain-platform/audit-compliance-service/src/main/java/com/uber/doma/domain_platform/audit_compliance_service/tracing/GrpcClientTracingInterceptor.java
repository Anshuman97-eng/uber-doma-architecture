package com.uber.doma.domain_platform.audit_compliance_service.tracing;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Client-side gRPC interceptor that injects the W3C {@code traceparent}
 * header into outgoing call metadata and starts a client span for the
 * outbound RPC. The interceptor delegates to the OpenTelemetry propagator
 * so the injected header is compatible with any other W3C-compliant
 * collector (Jaeger, Zipkin, OpenTelemetry Collector, etc.).
 */
public class GrpcClientTracingInterceptor implements ClientInterceptor {

    private static final Logger log = LoggerFactory.getLogger(GrpcClientTracingInterceptor.class);

    private final Tracer tracer;

    /**
     * Constructs a new instance that sources its {@link Tracer} from the
     * supplied {@link OpenTelemetry} SDK.
     *
     * @param openTelemetry OpenTelemetry SDK
     * @param serviceName   logical service name to attribute outbound spans to
     */
    public GrpcClientTracingInterceptor(final OpenTelemetry openTelemetry, final String serviceName) {
        if (openTelemetry == null) {
            throw new IllegalArgumentException("openTelemetry must not be null");
        }
        final String name = serviceName == null ? "audit-compliance-service" : serviceName;
        this.tracer = openTelemetry.getTracer(name);
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            final MethodDescriptor<ReqT, RespT> method,
            final CallOptions callOptions,
            final Channel next) {

        final String fullMethod = method.getFullMethodName();
        final Span span = tracer.spanBuilder(fullMethod)
                .setSpanKind(SpanKind.CLIENT)
                .setAttribute("rpc.system", "grpc")
                .setAttribute("rpc.service", method.getServiceName())
                .setAttribute("rpc.method", method.getBareMethodName())
                .startSpan();

        final Context spanContext = Context.current().with(span);
        if (log.isDebugEnabled()) {
            log.debug("[gRPC CLIENT] starting span {} trace={} method={}",
                    span.getSpanContext().getSpanId(),
                    span.getSpanContext().getTraceId(),
                    fullMethod);
        }

        final TraceContextMdcBinder.MdcSnapshot mdc = TraceContextMdcBinder.attach(span);
        return new TracingClientCall<>(next.newCall(method, callOptions), span, spanContext, mdc);
    }

    /**
     * Forwarding {@link ClientCall} that injects the W3C trace context into
     * outgoing headers on the first {@code sendMessage} call and closes the
     * span when the call is closed.
     */
    private static final class TracingClientCall<ReqT, RespT>
            extends ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT> {

        private final Span span;
        private final Context spanContext;
        private final TraceContextMdcBinder.MdcSnapshot mdc;
        private final TextMapSetter<Metadata> metadataSetter = new MetadataTextMapSetter();

        TracingClientCall(final ClientCall<ReqT, RespT> delegate,
                          final Span span,
                          final Context spanContext,
                          final TraceContextMdcBinder.MdcSnapshot mdc) {
            super(delegate);
            this.span = span;
            this.spanContext = spanContext;
            this.mdc = mdc;
        }

        @Override
        public void start(final Listener<RespT> responseListener, final Metadata headers) {
            try (Scope ignored = spanContext.makeCurrent()) {
                W3CTraceCarrier.INSTANCE.inject(spanContext, headers, metadataSetter);
                if (log.isDebugEnabled()) {
                    log.debug("[gRPC CLIENT] injected traceparent={}",
                            W3CTraceCarrier.INSTANCE.formatTraceparent(Span.fromContext(spanContext).getSpanContext()));
                }
                super.start(new TracingListener<>(responseListener, span, spanContext, mdc), headers);
            }
        }

        @Override
        public void sendMessage(final ReqT message) {
            try (Scope ignored = spanContext.makeCurrent()) {
                super.sendMessage(message);
            }
        }

        @Override
        public void halfClose() {
            try (Scope ignored = spanContext.makeCurrent()) {
                super.halfClose();
            }
        }
    }

    /**
     * Client listener wrapper that closes the span on terminal callbacks.
     */
    private static final class TracingListener<RespT> extends ClientCall.Listener<RespT> {

        private final ClientCall.Listener<RespT> delegate;
        private final Span span;
        private final Context spanContext;
        private final TraceContextMdcBinder.MdcSnapshot mdc;

        TracingListener(final ClientCall.Listener<RespT> delegate,
                        final Span span,
                        final Context spanContext,
                        final TraceContextMdcBinder.MdcSnapshot mdc) {
            this.delegate = delegate;
            this.span = span;
            this.spanContext = spanContext;
            this.mdc = mdc;
        }

        @Override
        public void onMessage(final RespT message) {
            try (Scope ignored = spanContext.makeCurrent()) {
                delegate.onMessage(message);
            }
        }

        @Override
        public void onClose(final Status status, final Metadata trailers) {
            try (Scope ignored = spanContext.makeCurrent()) {
                if (status != null && !status.isOk()) {
                    final String description = status.getDescription() == null
                            ? status.getCode().name()
                            : status.getDescription();
                    span.setStatus(StatusCode.ERROR, description);
                }
                delegate.onClose(status, trailers);
            } finally {
                span.end();
                TraceContextMdcBinder.detach(mdc);
            }
        }

        @Override
        public void onHeaders(final Metadata headers) {
            try (Scope ignored = spanContext.makeCurrent()) {
                delegate.onHeaders(headers);
            }
        }

        @Override
        public void onReady() {
            try (Scope ignored = spanContext.makeCurrent()) {
                delegate.onReady();
            }
        }
    }

    /**
     * Bridges a gRPC {@link Metadata} carrier to the OpenTelemetry
     * {@link TextMapSetter} contract. Header names are kept lower-case so
     * they round-trip through the {@link W3CTraceCarrier} unchanged.
     */
    private static final class MetadataTextMapSetter implements TextMapSetter<Metadata> {
        @Override
        public void set(final Metadata carrier, final String key, final String value) {
            if (carrier == null || key == null || value == null) {
                return;
            }
            carrier.put(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER), value);
        }
    }

    private static Map<String, String> copyIntoCarrier(final Metadata metadata) {
        final Map<String, String> carrier = new HashMap<>();
        for (final String key : metadata.keys()) {
            if (key == null) {
                continue;
            }
            final String value = metadata.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER));
            if (value != null) {
                carrier.put(key.toLowerCase(), value);
            }
        }
        return carrier;
    }
}