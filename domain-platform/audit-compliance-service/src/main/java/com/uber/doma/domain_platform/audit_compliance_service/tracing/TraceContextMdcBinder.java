package com.uber.doma.domain_platform.audit_compliance_service.tracing;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

/**
 * Bridges the OpenTelemetry active {@link io.opentelemetry.api.trace.Span}
 * into the SLF4J {@link MDC} so that log statements automatically carry the
 * W3C {@code traceId} and {@code spanId} fields required for end-to-end
 * distributed-trace correlation in Jaeger.
 *
 * <p>The bridge is intentionally explicit: every interceptor that opens or
 * closes a span must call {@link #attach(io.opentelemetry.api.trace.Span)} on
 * entry and {@link #detach()} on exit. The class is thread-safe because it
 * manipulates {@link MDC}, which is itself stored in a {@link ThreadLocal}.</p>
 */
public final class TraceContextMdcBinder {

    /**
     * Constant map key under which the previous MDC contents are stashed so
     * nested spans can be restored after the inner span closes.
     */
    public static final String MDC_PREVIOUS_TRACE_ID = "_auditPrevTraceId";
    public static final String MDC_PREVIOUS_SPAN_ID = "_auditPrevSpanId";
    public static final String MDC_PREVIOUS_PARENT_SPAN_ID = "_auditPrevParentSpanId";
    public static final String MDC_PREVIOUS_SERVICE_NAME = "_auditPrevServiceName";

    private TraceContextMdcBinder() {
    }

    /**
     * Push the identity of the supplied span onto the SLF4J MDC. Existing
     * {@code traceId} / {@code spanId} entries are saved so that nested
     * spans correctly restore the parent when they close.
     *
     * @param span span to publish into the MDC; may be {@code null}
     * @return a {@link MdcSnapshot} that {@link #detach(MdcSnapshot)} will use to roll back
     */
    public static MdcSnapshot attach(final Span span) {
        final MdcSnapshot snapshot = capturePrevious();
        final SpanContext ctx = span == null ? null : span.getSpanContext();
        if (ctx == null || !ctx.isValid()) {
            return snapshot;
        }
        MDC.put(TracingConstants.MDC_TRACE_ID, ctx.getTraceId());
        MDC.put(TracingConstants.MDC_SPAN_ID, ctx.getSpanId());
        if (ctx.isRemote()) {
            MDC.put(TracingConstants.MDC_PARENT_SPAN_ID, ctx.getSpanId());
        }
        return snapshot;
    }

    /**
     * Convenience variant that publishes the current {@link Span#current()}.
     */
    public static MdcSnapshot attachCurrent() {
        return attach(Span.current());
    }

    /**
     * Restore the MDC entries that were in place before {@link #attach(Span)}.
     *
     * @param snapshot snapshot returned by the matching attach call; may be {@code null}
     */
    public static void detach(final MdcSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        restore(snapshot.previousTraceId, TracingConstants.MDC_TRACE_ID);
        restore(snapshot.previousSpanId, TracingConstants.MDC_SPAN_ID);
        restore(snapshot.previousParentSpanId, TracingConstants.MDC_PARENT_SPAN_ID);
        restore(snapshot.previousServiceName, TracingConstants.MDC_SERVICE_NAME);
    }

    /**
     * Clear trace-related entries from the MDC.
     */
    public static void clear() {
        MDC.remove(TracingConstants.MDC_TRACE_ID);
        MDC.remove(TracingConstants.MDC_SPAN_ID);
        MDC.remove(TracingConstants.MDC_PARENT_SPAN_ID);
    }

    private static void restore(final String previousValue, final String key) {
        if (previousValue == null) {
            MDC.remove(key);
        } else {
            MDC.put(key, previousValue);
        }
    }

    private static MdcSnapshot capturePrevious() {
        return new MdcSnapshot(
                MDC.get(TracingConstants.MDC_TRACE_ID),
                MDC.get(TracingConstants.MDC_SPAN_ID),
                MDC.get(TracingConstants.MDC_PARENT_SPAN_ID),
                MDC.get(TracingConstants.MDC_SERVICE_NAME));
    }

    /**
     * Snapshot of the MDC entries that were in place before a span was
     * attached, allowing them to be restored when the span closes.
     */
    public static final class MdcSnapshot {
        private final String previousTraceId;
        private final String previousSpanId;
        private final String previousParentSpanId;
        private final String previousServiceName;

        MdcSnapshot(final String previousTraceId,
                    final String previousSpanId,
                    final String previousParentSpanId,
                    final String previousServiceName) {
            this.previousTraceId = previousTraceId;
            this.previousSpanId = previousSpanId;
            this.previousParentSpanId = previousParentSpanId;
            this.previousServiceName = previousServiceName;
        }

        public Map<String, String> asMap() {
            final Map<String, String> map = new HashMap<>(4);
            map.put(TracingConstants.MDC_TRACE_ID, previousTraceId);
            map.put(TracingConstants.MDC_SPAN_ID, previousSpanId);
            map.put(TracingConstants.MDC_PARENT_SPAN_ID, previousParentSpanId);
            map.put(TracingConstants.MDC_SERVICE_NAME, previousServiceName);
            return map;
        }
    }
}