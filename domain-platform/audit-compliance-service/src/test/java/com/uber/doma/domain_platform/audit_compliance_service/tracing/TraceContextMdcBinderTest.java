package com.uber.doma.domain_platform.audit_compliance_service.tracing;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link TraceContextMdcBinder}.
 *
 * <p>These tests verify that attaching a span populates the SLF4J MDC and
 * that detaching restores the prior MDC state so nested spans do not bleed
 * trace ids into each other.</p>
 */
class TraceContextMdcBinderTest {

    private static final String TRACE_ID = "0af7651916cd43dd8448eb211c80319c";
    private static final String SPAN_ID = "b7ad6b7169203331";

    @BeforeEach
    void clearMdc() {
        TraceContextMdcBinder.clear();
        MDC.clear();
    }

    @AfterEach
    void cleanupMdc() {
        TraceContextMdcBinder.clear();
        MDC.clear();
    }

    @Test
    void attachPopulatesMdcFromSpan() {
        final SpanContext ctx = SpanContext.createFromRemoteParent(TRACE_ID, SPAN_ID,
                TraceFlags.getSampled(), TraceState.getDefault());
        final Span span = Span.wrap(ctx);
        final TraceContextMdcBinder.MdcSnapshot snapshot = TraceContextMdcBinder.attach(span);
        try {
            assertEquals(TRACE_ID, MDC.get(TracingConstants.MDC_TRACE_ID));
            assertEquals(SPAN_ID, MDC.get(TracingConstants.MDC_SPAN_ID));
        } finally {
            TraceContextMdcBinder.detach(snapshot);
        }
    }

    @Test
    void detachRestoresPreviousMdcState() {
        final SpanContext ctx = SpanContext.createFromRemoteParent(TRACE_ID, SPAN_ID,
                TraceFlags.getSampled(), TraceState.getDefault());
        MDC.put(TracingConstants.MDC_TRACE_ID, "existing-trace");
        MDC.put(TracingConstants.MDC_SPAN_ID, "existing-span");

        final TraceContextMdcBinder.MdcSnapshot snapshot = TraceContextMdcBinder.attach(Span.wrap(ctx));
        assertEquals(TRACE_ID, MDC.get(TracingConstants.MDC_TRACE_ID));
        TraceContextMdcBinder.detach(snapshot);
        assertEquals("existing-trace", MDC.get(TracingConstants.MDC_TRACE_ID));
        assertEquals("existing-span", MDC.get(TracingConstants.MDC_SPAN_ID));
    }

    @Test
    void detachRemovesEntriesWhenNothingWasPresent() {
        final SpanContext ctx = SpanContext.createFromRemoteParent(TRACE_ID, SPAN_ID,
                TraceFlags.getSampled(), TraceState.getDefault());
        final TraceContextMdcBinder.MdcSnapshot snapshot = TraceContextMdcBinder.attach(Span.wrap(ctx));
        TraceContextMdcBinder.detach(snapshot);
        assertNull(MDC.get(TracingConstants.MDC_TRACE_ID));
        assertNull(MDC.get(TracingConstants.MDC_SPAN_ID));
    }

    @Test
    void nestedAttachActRestoresOuterOnRunt() {
        final SpanContext outer = SpanContext.createFromRemoteParent(TRACE_ID, "0000000000000001",
                TraceFlags.getSampled(), TraceState.getDefault());
        final SpanContext inner = SpanContext.createFromRemoteParent(TRACE_ID, "0000000000000002",
                TraceFlags.getSampled(), TraceState.getDefault());

        final TraceContextMdcBinder.MdcSnapshot outerSnap = TraceContextMdcBinder.attach(Span.wrap(outer));
        try {
            assertEquals("0000000000000001", MDC.get(TracingConstants.MDC_SPAN_ID));
            final TraceContextMdcBinder.MdcSnapshot innerSnap = TraceContextMdcBinder.attach(Span.wrap(inner));
            try {
                assertEquals("0000000000000002", MDC.get(TracingConstants.MDC_SPAN_ID));
            } finally {
                TraceContextMdcBinder.detach(innerSnap);
            }
            assertEquals("0000000000000001", MDC.get(TracingConstants.MDC_SPAN_ID));
        } finally {
            TraceContextMdcBinder.detach(outerSnap);
        }
        assertNull(MDC.get(TracingConstants.MDC_SPAN_ID));
    }

    @Test
    void attachInvalidSpanDoesNotPopulateMdc() {
        final TraceContextMdcBinder.MdcSnapshot snapshot = TraceContextMdcBinder.attach(Span.getInvalid());
        try {
            assertNull(MDC.get(TracingConstants.MDC_TRACE_ID));
            assertNull(MDC.get(TracingConstants.MDC_SPAN_ID));
        } finally {
            TraceContextMdcBinder.detach(snapshot);
        }
    }

    @Test
    void attachNullDoesNotPopulateMdc() {
        final TraceContextMdcBinder.MdcSnapshot snapshot = TraceContextMdcBinder.attach(null);
        try {
            assertNull(MDC.get(TracingConstants.MDC_TRACE_ID));
        } finally {
            TraceContextMdcBinder.detach(snapshot);
        }
    }

    @Test
    void clearRemovesAllTracedEntries() {
        final SpanContext ctx = SpanContext.createFromRemoteParent(TRACE_ID, SPAN_ID,
                TraceFlags.getSampled(), TraceState.getDefault());
        MDC.put(TracingConstants.MDC_TRACE_ID, TRACE_ID);
        MDC.put(TracingConstants.MDC_SPAN_ID, SPAN_ID);
        MDC.put("custom", "value");
        TraceContextMdcBinder.clear();
        assertNull(MDC.get(TracingConstants.MDC_TRACE_ID));
        assertNull(MDC.get(TracingConstants.MDC_SPAN_ID));
        // clear() should not nuke unrelated MDC entries.
        assertEquals("value", MDC.get("custom"));
        // SpanContext assertion above ensures the import path is exercised.
        assertNotNull(ctx);
        assertFalse(!ctx.isValid());
    }

    @Test
    void attachCurrentUsesCurrentSpan() {
        final SpanContext ctx = SpanContext.createFromRemoteParent(TRACE_ID, SPAN_ID,
                TraceFlags.getSampled(), TraceState.getDefault());
        final Span span = Span.wrap(ctx);
        try (Scope ignored = span.makeCurrent()) {
            final TraceContextMdcBinder.MdcSnapshot snapshot = TraceContextMdcBinder.attachCurrent();
            try {
                assertEquals(TRACE_ID, MDC.get(TracingConstants.MDC_TRACE_ID));
                assertEquals(SPAN_ID, MDC.get(TracingConstants.MDC_SPAN_ID));
            } finally {
                TraceContextMdcBinder.detach(snapshot);
            }
        }
    }

    @Test
    void snapshotAsMapExposesPreviousValues() {
        MDC.put(TracingConstants.MDC_TRACE_ID, "t");
        MDC.put(TracingConstants.MDC_SPAN_ID, "s");
        MDC.put(TracingConstants.MDC_PARENT_SPAN_ID, "p");
        MDC.put(TracingConstants.MDC_SERVICE_NAME, "svc");

        final TraceContextMdcBinder.MdcSnapshot snapshot = TraceContextMdcBinder.attach(Span.wrap(
                SpanContext.createFromRemoteParent(TRACE_ID, SPAN_ID,
                        TraceFlags.getSampled(), TraceState.getDefault())));
        final var asMap = snapshot.asMap();
        assertEquals("t", asMap.get(TracingConstants.MDC_TRACE_ID));
        assertEquals("s", asMap.get(TracingConstants.MDC_SPAN_ID));
        assertEquals("p", asMap.get(TracingConstants.MDC_PARENT_SPAN_ID));
        assertEquals("svc", asMap.get(TracingConstants.MDC_SERVICE_NAME));
        TraceContextMdcBinder.detach(snapshot);
    }
}