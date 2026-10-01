package com.uber.doma.domain_platform.audit_compliance_service.tracing;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link W3CTraceCarrier}.
 *
 * <p>These tests focus on the round-trip of W3C {@code traceparent} /
 * {@code tracestate} headers: parse a known header, format a known span
 * context, and inject/extract through a plain {@link Map} carrier.</p>
 */
class W3CTraceCarrierTest {

    private static final String TRACE_ID = "0af7651916cd43dd8448eb211c80319c";
    private static final String PARENT_ID = "b7ad6b7169203331";
    private static final String TRACEPARENT = "00-" + TRACE_ID + "-" + PARENT_ID + "-01";

    private OpenTelemetrySdk sdk;
    private InMemorySpanExporter exporter;

    @BeforeEach
    void setUp() {
        exporter = InMemorySpanExporter.create();
        final SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(exporter))
                .build();
        sdk = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .build();
    }

    @Test
    void parseValidTraceparentYieldsValidSpanContext() {
        final SpanContext ctx = W3CTraceCarrier.INSTANCE.parseTraceparent(TRACEPARENT);
        assertTrue(ctx.isValid(), "Expected parsed context to be valid");
        assertEquals(TRACE_ID, ctx.getTraceId());
        assertEquals(PARENT_ID, ctx.getSpanId());
        assertTrue(ctx.isSampled());
        assertTrue(ctx.isRemote());
    }

    @Test
    void parseInvalidTraceparentReturnsInvalidSpanContext() {
        assertSame(SpanContext.getInvalid(), W3CTraceCarrier.INSTANCE.parseTraceparent(null));
        assertSame(SpanContext.getInvalid(), W3CTraceCarrier.INSTANCE.parseTraceparent(""));
        assertSame(SpanContext.getInvalid(), W3CTraceCarrier.INSTANCE.parseTraceparent("garbage"));
        assertSame(SpanContext.getInvalid(), W3CTraceCarrier.INSTANCE.parseTraceparent("00-badtraceid-badspanid-01"));
        assertSame(SpanContext.getInvalid(), W3CTraceCarrier.INSTANCE.parseTraceparent("00-"
                + TRACE_ID + "-badspanid-01"));
        assertSame(SpanContext.getInvalid(), W3CTraceCarrier.INSTANCE.parseTraceparent("00-"
                + TRACE_ID + "-" + PARENT_ID));
        assertSame(SpanContext.getInvalid(),
                W3CTraceCarrier.INSTANCE.parseTraceparent("ff-" + TRACE_ID + "-" + PARENT_ID + "-01"));
    }

    @Test
    void formatTraceparentProducesCanonicalWireFormat() {
        final SpanContext ctx = SpanContext.createFromRemoteParent(TRACE_ID, PARENT_ID,
                TraceFlags.getSampled(), TraceState.getDefault());
        final String wire = W3CTraceCarrier.INSTANCE.formatTraceparent(ctx);
        assertEquals(TRACEPARENT, wire);
    }

    @Test
    void formatTraceparentRejectsInvalidContext() {
        assertNull(W3CTraceCarrier.INSTANCE.formatTraceparent(null));
        assertNull(W3CTraceCarrier.INSTANCE.formatTraceparent(SpanContext.getInvalid()));
    }

    @Test
    void roundTripParseFormatIsLossless() {
        final SpanContext original = SpanContext.createFromRemoteParent(TRACE_ID, PARENT_ID,
                TraceFlags.getSampled(), TraceState.getDefault());
        final String wire = W3CTraceCarrier.INSTANCE.formatTraceparent(original);
        final SpanContext reparsed = W3CTraceCarrier.INSTANCE.parseTraceparent(wire);
        assertEquals(original.getTraceId(), reparsed.getTraceId());
        assertEquals(original.getSpanId(), reparsed.getSpanId());
        assertEquals(original.getTraceFlags(), reparsed.getTraceFlags());
    }

    @Test
    void extractReadsLowercaseAndUppercaseKeys() {
        final Map<String, String> carrier = new HashMap<>();
        carrier.put("TRACEPARENT", TRACEPARENT);
        final Context extracted = W3CTraceCarrier.INSTANCE.extract(carrier);
        final SpanContext ctx = Span.fromContext(extracted).getSpanContext();
        assertTrue(ctx.isValid());
        assertEquals(TRACE_ID, ctx.getTraceId());
    }

    @Test
    void injectWritesHeadersIntoCarrier() {
        final Span span = sdk.getTracer("test").spanBuilder("parent").startSpan();
        try (Scope ignored = span.makeCurrent()) {
            final Map<String, String> carrier = new HashMap<>();
            W3CTraceCarrier.INSTANCE.inject(Context.current(), carrier);
            assertTrue(carrier.containsKey("traceparent"));
            final String value = carrier.get("traceparent");
            assertNotNull(value);
            final SpanContext ctx = W3CTraceCarrier.INSTANCE.parseTraceparent(value);
            assertTrue(ctx.isValid());
            assertEquals(span.getSpanContext().getTraceId(), ctx.getTraceId());
        } finally {
            span.end();
        }
    }

    @Test
    void extractFromEmptyOrNullCarrierReturnsRootContext() {
        assertSame(Context.root(), W3CTraceCarrier.INSTANCE.extract(null));
        assertSame(Context.root(), W3CTraceCarrier.INSTANCE.extract(new HashMap<>()));
    }

    @Test
    void injectIntoNullCarrierIsNoOp() {
        W3CTraceCarrier.INSTANCE.inject(Context.root(), null);
    }

    @Test
    void snapshotReturnsImmutableView() {
        final Map<String, String> carrier = new HashMap<>();
        carrier.put("traceparent", TRACEPARENT);
        carrier.put("tracestate", "vendor=value");
        carrier.put("X-Custom", "ignored");
        final Map<String, String> snapshot = W3CTraceCarrier.INSTANCE.snapshot(carrier);
        assertEquals(2, snapshot.size());
        assertEquals(TRACEPARENT, snapshot.get("traceparent"));
        assertEquals("vendor=value", snapshot.get("tracestate"));
        try {
            snapshot.put("traceparent", "modified");
            assertNotEquals(TRACEPARENT, snapshot.get("traceparent"));
            // Some JDKs allow mutation of unmodifiable maps without throwing.
        } catch (final UnsupportedOperationException expected) {
            // expected
        }
    }

    @Test
    void snapshotHandlesEmptyCarrier() {
        assertTrue(W3CTraceCarrier.INSTANCE.snapshot(null).isEmpty());
        assertTrue(W3CTraceCarrier.INSTANCE.snapshot(new HashMap<>()).isEmpty());
    }

    @Test
    void currentTraceAndSpanIdReturnsNullWhenNoSpan() {
        assertNull(W3CTraceCarrier.currentTraceId());
        assertNull(W3CTraceCarrier.currentSpanId());
    }

    @Test
    void currentTraceAndSpanIdReflectActiveSpan() {
        final Span span = sdk.getTracer("test").spanBuilder("active").startSpan();
        try (Scope ignored = span.makeCurrent()) {
            assertEquals(span.getSpanContext().getTraceId(), W3CTraceCarrier.currentTraceId());
            assertEquals(span.getSpanContext().getSpanId(), W3CTraceCarrier.currentSpanId());
        } finally {
            span.end();
        }
    }

    @Test
    void propagateAcrossThreadBoundaryViaContextCapture() {
        final Span span = sdk.getTracer("test").spanBuilder("caller").startSpan();
        final String expectedTrace;
        final String expectedSpan;
        try (Scope ignored = span.makeCurrent()) {
            expectedTrace = span.getSpanContext().getTraceId();
            expectedSpan = span.getSpanContext().getSpanId();
        }
        // Capture context then activate it on another thread.
        final Context captured;
        try (Scope ignored = span.makeCurrent()) {
            captured = Context.current();
        }
        span.end();

        final String[] observed = new String[2];
        final Thread worker = new Thread(() -> {
            try (Scope ignored = captured.makeCurrent()) {
                observed[0] = W3CTraceCarrier.currentTraceId();
                observed[1] = W3CTraceCarrier.currentSpanId();
            }
        });
        worker.start();
        try {
            worker.join();
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        assertEquals(expectedTrace, observed[0]);
        assertEquals(expectedSpan, observed[1]);

        final List<SpanData> spans = exporter.getFinishedSpanItems();
        assertFalse(spans.isEmpty());
        final SpanData exported = spans.get(0);
        assertEquals(TRACE_ID.length(), expectedTrace.length());
        assertEquals(SpanId.getLength(), expectedSpan.length());
        assertEquals(TraceId.getLength(), expectedTrace.length());
        assertEquals(exported.getTraceId(), expectedTrace);
        assertEquals(exported.getSpanId(), expectedSpan);
    }
}