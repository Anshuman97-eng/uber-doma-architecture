package com.uber.doma.domain_platform.audit_compliance_service.tracing;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Adapter around the OpenTelemetry {@link W3CTraceContextPropagator} that
 * operates on plain {@link Map} carriers. The wrapper is the single seam
 * where the W3C {@code traceparent} / {@code tracestate} wire format is
 * parsed and emitted, so it can be reused by the gRPC server interceptor,
 * the gRPC client interceptor, and any future HTTP or messaging bridge.
 */
public final class W3CTraceCarrier {

    /**
     * Singleton delegating to the OpenTelemetry W3C reference propagator.
     */
    public static final W3CTraceCarrier INSTANCE = new W3CTraceCarrier();

    private static final W3CTraceContextPropagator DELEGATE = W3CTraceContextPropagator.getInstance();

    /**
     * Map-backed getter usable with the OpenTelemetry propagator API.
     */
    public static final TextMapGetter<Map<String, String>> MAP_GETTER = new MapTextMapGetter();

    /**
     * Map-backed setter usable with the OpenTelemetry propagator API.
     */
    public static final TextMapSetter<Map<String, String>> MAP_SETTER = new MapTextMapSetter();

    private W3CTraceCarrier() {
    }

    /**
     * Extract a {@link Context} from a {@code Map} of carrier headers using the
     * W3C Trace Context wire format.
     *
     * @param carrier map of carrier headers; may be {@code null}
     * @return populated OpenTelemetry context (root if carrier is null/empty)
     */
    public Context extract(final Map<String, String> carrier) {
        if (carrier == null || carrier.isEmpty()) {
            return Context.root();
        }
        return DELEGATE.extract(Context.root(), carrier, MAP_GETTER);
    }

    /**
     * Inject the current OpenTelemetry context into a {@code Map} of carrier
     * headers using the W3C Trace Context wire format.
     *
     * @param context context to inject; if {@code null}, the current context is used
     * @param carrier carrier map that will receive the propagated headers
     */
    public void inject(final Context context, final Map<String, String> carrier) {
        if (carrier == null) {
            return;
        }
        final Context ctx = context == null ? Context.current() : context;
        DELEGATE.inject(ctx, carrier, MAP_SETTER);
    }

    /**
     * Inject the current OpenTelemetry context into an arbitrary carrier using a
     * caller-supplied {@link TextMapSetter}. This is the right hook for any
     * transport that does not expose a {@code Map}-shaped carrier (gRPC
     * {@link io.grpc.Metadata}, Kafka headers, etc.).
     *
     * @param context context to inject; if {@code null}, the current context is used
     * @param carrier carrier that will receive the propagated headers
     * @param setter  setter implementation that writes into the carrier
     * @param <C>     carrier type
     */
    public <C> void inject(final Context context, final C carrier, final TextMapSetter<C> setter) {
        if (carrier == null || setter == null) {
            return;
        }
        final Context ctx = context == null ? Context.current() : context;
        DELEGATE.inject(ctx, carrier, setter);
    }

    /**
     * Parse a W3C {@code traceparent} header value into a {@link SpanContext}.
     *
     * <p>Expected format: {@code <version>-<trace-id>-<parent-id>-<trace-flags>}.
     * Returns the invalid span context if any segment fails validation.</p>
     *
     * @param traceparent the raw header value; may be {@code null}
     * @return valid {@link SpanContext} or {@link SpanContext#getInvalid() invalid} context
     */
    public SpanContext parseTraceparent(final String traceparent) {
        if (traceparent == null || traceparent.isEmpty()) {
            return SpanContext.getInvalid();
        }
        final String[] parts = traceparent.split("-");
        if (parts.length != 4) {
            return SpanContext.getInvalid();
        }
        final String version = parts[0];
        final String traceId = parts[1];
        final String parentId = parts[2];
        final String flags = parts[3];

        // Per the W3C Trace Context spec, the only currently-supported version
        // is "00". Future versions are intentionally rejected here so that
        // callers fall back to creating a new trace rather than silently
        // accepting a wire format we do not understand.
        if (!"00".equals(version)) {
            return SpanContext.getInvalid();
        }
        if (!TraceId.isValid(traceId)) {
            return SpanContext.getInvalid();
        }
        if (!SpanId.isValid(parentId)) {
            return SpanContext.getInvalid();
        }
        final byte traceFlagsByte = parseFlags(flags);
        final TraceState traceState = TraceState.getDefault();
        return SpanContext.createFromRemoteParent(traceId, parentId, TraceFlags.fromByte(traceFlagsByte),
                traceState);
    }

    /**
     * Format a {@link SpanContext} into the canonical W3C {@code traceparent}
     * header value (00-{traceId}-{spanId}-{flags}).
     *
     * @param spanContext the context to format
     * @return W3C traceparent string, or {@code null} if the context is invalid
     */
    public String formatTraceparent(final SpanContext spanContext) {
        if (spanContext == null || !spanContext.isValid()) {
            return null;
        }
        final String flags = spanContext.isSampled() ? "01" : "00";
        return String.format("00-%s-%s-%s", spanContext.getTraceId(), spanContext.getSpanId(), flags);
    }

    /**
     * Build an immutable snapshot of the W3C headers present in the supplied
     * carrier map.
     *
     * @param carrier map of carrier headers
     * @return immutable snapshot of {@code traceparent} and {@code tracestate} entries
     */
    public Map<String, String> snapshot(final Map<String, String> carrier) {
        if (carrier == null || carrier.isEmpty()) {
            return Collections.emptyMap();
        }
        final Map<String, String> snapshot = new HashMap<>(2);
        final String tp = carrier.get(TracingConstants.TRACEPARENT_HEADER);
        if (tp != null) {
            snapshot.put(TracingConstants.TRACEPARENT_HEADER, tp);
        }
        final String ts = carrier.get(TracingConstants.TRACESTATE_HEADER);
        if (ts != null) {
            snapshot.put(TracingConstants.TRACESTATE_HEADER, ts);
        }
        return Collections.unmodifiableMap(snapshot);
    }

    private static byte parseFlags(final String flags) {
        if (flags == null || flags.isEmpty()) {
            return 0;
        }
        try {
            return (byte) (Integer.parseInt(flags, 16) & 0xFF);
        } catch (final NumberFormatException ex) {
            return 0;
        }
    }

    /**
     * Case-insensitive {@link TextMapGetter} backed by a {@link Map}.
     */
    private static final class MapTextMapGetter implements TextMapGetter<Map<String, String>> {
        @Override
        public Iterable<String> keys(final Map<String, String> carrier) {
            return carrier.keySet();
        }

        @Override
        public String get(final Map<String, String> carrier, final String key) {
            if (carrier == null || key == null) {
                return null;
            }
            final String direct = carrier.get(key);
            if (direct != null) {
                return direct;
            }
            for (final Map.Entry<String, String> entry : carrier.entrySet()) {
                if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                    return entry.getValue();
                }
            }
            return null;
        }
    }

    /**
     * Case-preserving {@link TextMapSetter} that writes into a {@link Map}.
     */
    private static final class MapTextMapSetter implements TextMapSetter<Map<String, String>> {
        @Override
        public void set(final Map<String, String> carrier, final String key, final String value) {
            if (carrier == null || key == null) {
                return;
            }
            carrier.put(key, value);
        }
    }

    /**
     * Convenience helper for code paths that want the trace id of the current span.
     *
     * @return current trace id, or {@code null} when no span is active
     */
    public static String currentTraceId() {
        final Span current = Span.current();
        final SpanContext ctx = current.getSpanContext();
        return ctx.isValid() ? ctx.getTraceId() : null;
    }

    /**
     * Convenience helper for code paths that want the span id of the current span.
     *
     * @return current span id, or {@code null} when no span is active
     */
    public static String currentSpanId() {
        final Span current = Span.current();
        final SpanContext ctx = current.getSpanContext();
        return ctx.isValid() ? ctx.getSpanId() : null;
    }
}