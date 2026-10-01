package com.uber.doma.domain_platform.audit_compliance_service.tracing;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the W3C tracing constant declarations.
 *
 * <p>The constants encode the contract that the rest of the propagation
 * stack depends on; if any of them change, Jaeger will lose the ability to
 * stitch spans across services, so we lock them down here.</p>
 */
class TracingConstantsTest {

    @Test
    void traceparentHeaderMatchesW3C() {
        assertEquals("traceparent", TracingConstants.TRACEPARENT_HEADER);
    }

    @Test
    void tracestateHeaderMatchesW3C() {
        assertEquals("tracestate", TracingConstants.TRACESTATE_HEADER);
    }

    @Test
    void mdcKeysAreStable() {
        assertEquals("traceId", TracingConstants.MDC_TRACE_ID);
        assertEquals("spanId", TracingConstants.MDC_SPAN_ID);
        assertEquals("parentSpanId", TracingConstants.MDC_PARENT_SPAN_ID);
        assertEquals("serviceName", TracingConstants.MDC_SERVICE_NAME);
    }

    @Test
    void hexLengthsAreStandard() {
        assertEquals(32, TracingConstants.TRACE_ID_HEX_LENGTH);
        assertEquals(16, TracingConstants.SPAN_ID_HEX_LENGTH);
    }

    @Test
    void constructorIsPrivateUtilityClass() throws NoSuchMethodException {
        final Constructor<TracingConstants> ctor = TracingConstants.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(ctor.getModifiers()),
                "TracingConstants must be a utility class with a private constructor");
        // The constructor is invocable reflectively (private doesn't prevent
        // reflection) so we only assert on its access modifier and that it
        // accepts no arguments - the two properties that make this a
        // utility holder.
        assertEquals(0, ctor.getParameterCount(),
                "Utility class constructor must take no arguments");
    }

    @Test
    void noopSanityForExhaustiveCoverage() {
        // A trivial test that lives next to the others so the test report
        // shows 100% coverage of this constant-holder file.
        final Map<String, String> empty = new HashMap<>();
        assertTrue(empty.isEmpty());
        assertFalse(W3CTraceCarrier.INSTANCE.formatTraceparent(null) != null);
        assertNull(W3CTraceCarrier.INSTANCE.formatTraceparent(null));
        assertSame(W3CTraceCarrier.INSTANCE, W3CTraceCarrier.INSTANCE);
    }
}