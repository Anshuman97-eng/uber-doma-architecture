package com.uber.doma.domain_platform.audit_compliance_service.tracing;

import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Server-side gRPC interceptor that extracts the W3C {@code traceparent}
 * header from incoming metadata, starts a server span bound to the supplied
 * OpenTelemetry {@link Tracer}, and pushes the trace identity into the
 * SLF4J {@link org.slf4j.MDC} so that downstream log statements are
 * correlated with the originating trace.
 *
 * <p>Spans are started and closed in a try-with-resources block by wrapping
 * the listener so the {@link Scope} is entered on the calling Netty event-loop
 * thread for every listener callback, ensuring that downstream executors
 * inheriting the trace context continue to see the correct trace and span.</p>
 */
public class GrpcServerTracingInterceptor implements ServerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(GrpcServerTracingInterceptor.class);

    private final Tracer tracer;
    private final String serviceName;

    /**
     * @param openTelemetry OpenTelemetry SDK to source the {@link Tracer} from
     * @param serviceName   logical service name to record on every server span
     */
    public GrpcServerTracingInterceptor(final OpenTelemetry openTelemetry, final String serviceName) {
        if (openTelemetry == null) {
            throw new IllegalArgumentException("openTelemetry must not be null");
        }
        final String name = serviceName == null ? "audit-compliance-service" : serviceName;
        this.tracer = openTelemetry.getTracer(name);
        this.serviceName = name;
    }

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(
            final ServerCall<ReqT, RespT> call,
            final Metadata headers,
            final ServerCallHandler<ReqT, RespT> next) {

        final Map<String, String> carrier = toLowerCaseCarrier(headers);
        final Context extracted = W3CTraceCarrier.INSTANCE.extract(carrier);

        final String method = call.getMethodDescriptor().getFullMethodName();
        final Span span = tracer.spanBuilder(method)
                .setSpanKind(SpanKind.SERVER)
                .setParent(extracted)
                .setAttribute("rpc.system", "grpc")
                .setAttribute("rpc.service", call.getMethodDescriptor().getServiceName())
                .setAttribute("rpc.method", call.getMethodDescriptor().getBareMethodName())
                .setAttribute("service.name", serviceName)
                .startSpan();

        if (log.isDebugEnabled()) {
            log.debug("[gRPC SERVER] traceparent={} method={} spanId={}",
                    W3CTraceCarrier.INSTANCE.formatTraceparent(span.getSpanContext()),
                    method,
                    span.getSpanContext().getSpanId());
        }

        final Context spanContext = Context.current().with(span);
        final TraceContextMdcBinder.MdcSnapshot mdc = TraceContextMdcBinder.attach(span);

        return new SpanBindingListener<>(next.startCall(new StatusRecordingServerCall<>(call, span), headers),
                span, spanContext, mdc);
    }

    private static Map<String, String> toLowerCaseCarrier(final Metadata headers) {
        if (headers == null) {
            return new HashMap<>();
        }
        final Map<String, String> carrier = new HashMap<>();
        for (final String key : headers.keys()) {
            if (key == null) {
                continue;
            }
            final Metadata.Key<String> typedKey = Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER);
            final String value = headers.get(typedKey);
            if (value != null) {
                carrier.put(key.toLowerCase(), value);
            }
        }
        return carrier;
    }

    /**
     * Forwarding {@link ServerCall} that records exceptions and error status
     * codes onto the active span so they show up in Jaeger.
     */
    private static final class StatusRecordingServerCall<ReqT, RespT>
            extends ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT> {

        private final Span span;

        StatusRecordingServerCall(final ServerCall<ReqT, RespT> delegate, final Span span) {
            super(delegate);
            this.span = span;
        }

        @Override
        public void close(final Status status, final Metadata trailers) {
            if (status != null && !status.isOk()) {
                final String description = status.getDescription() == null
                        ? status.getCode().name()
                        : status.getDescription();
                span.setStatus(StatusCode.ERROR, description);
            }
            super.close(status, trailers);
        }
    }

    /**
     * Listener that opens an OpenTelemetry {@link Scope} around every
     * listener method invocation. The scope is entered on the calling thread
     * (which is usually a Netty event-loop thread for gRPC server calls) and
     * restored on exit, ensuring that downstream executors inheriting the
     * trace context continue to see the correct trace and span.
     */
    private static final class SpanBindingListener<ReqT> extends Listener<ReqT> {

        private final Listener<ReqT> delegate;
        private final Span span;
        private final Context spanContext;
        private final TraceContextMdcBinder.MdcSnapshot mdc;

        SpanBindingListener(final Listener<ReqT> delegate,
                            final Span span,
                            final Context spanContext,
                            final TraceContextMdcBinder.MdcSnapshot mdc) {
            this.delegate = delegate;
            this.span = span;
            this.spanContext = spanContext;
            this.mdc = mdc;
        }

        @Override
        public void onMessage(final ReqT message) {
            try (Scope ignored = spanContext.makeCurrent()) {
                delegate.onMessage(message);
            }
        }

        @Override
        public void onHalfClose() {
            try (Scope ignored = spanContext.makeCurrent()) {
                delegate.onHalfClose();
            }
        }

        @Override
        public void onCancel() {
            try (Scope ignored = spanContext.makeCurrent()) {
                span.setStatus(StatusCode.ERROR, "cancelled by client");
                delegate.onCancel();
            } finally {
                span.end();
                TraceContextMdcBinder.detach(mdc);
            }
        }

        @Override
        public void onComplete() {
            log.debug("[gRPC SERVER] onComplete: ending span {} (class={})",
                    span.getSpanContext().getSpanId(), span.getClass().getName());
            try (Scope ignored = spanContext.makeCurrent()) {
                delegate.onComplete();
            } finally {
                try {
                    span.end();
                    log.debug("[gRPC SERVER] span ended: {} spanClass={} isRecording={}",
                            span.getSpanContext().getSpanId(), span.getClass().getName(),
                            span.isRecording());
                } catch (final RuntimeException ex) {
                    log.warn("[gRPC SERVER] Failed to end span: {}", ex.getMessage(), ex);
                }
                TraceContextMdcBinder.detach(mdc);
            }
        }

        @Override
        public void onReady() {
            try (Scope ignored = spanContext.makeCurrent()) {
                delegate.onReady();
            }
        }
    }
}