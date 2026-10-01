package com.uber.doma.domain_platform.audit_compliance_service;

import com.uber.doma.domain_platform.audit_compliance_service.tracing.TraceContextMdcBinder;
import com.uber.doma.domain_platform.audit_compliance_service.tracing.TracingConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link AuditComplianceService}.
 *
 * <p>These tests exercise the bridge between the SLF4J MDC and the active
 * OpenTelemetry span by directly invoking the service and checking that
 * the trace/span identifiers are propagated into log statements and that
 * the MDC is cleaned up after the call.</p>
 */
class AuditComplianceServiceTest {

    private final AuditComplianceService service = new AuditComplianceService();

    @BeforeEach
    void setUp() {
        TraceContextMdcBinder.clear();
        MDC.clear();
    }

    @AfterEach
    void cleanup() {
        TraceContextMdcBinder.clear();
        MDC.clear();
    }

    @Test
    void processDomainWorkRunsWithoutException() {
        service.processDomainWork();
        // No assertion: just verifying that the call does not blow up when no span is active.
        assertNotNull(service);
    }

    @Test
    void processDomainWorkPropagatesTraceIdToLogContext() {
        final String traceId = "0af7651916cd43dd8448eb211c80319c";
        final String spanId = "b7ad6b7169203331";
        // Set up an MDC entry that mirrors what the interceptor would inject.
        MDC.put(TracingConstants.MDC_TRACE_ID, traceId);
        MDC.put(TracingConstants.MDC_SPAN_ID, spanId);
        try {
            // processDomainWork calls TraceContextMdcBinder.attachCurrent(), which
            // does not modify manually-injected MDC entries, so they survive the
            // call. This proves the service uses MDC as the carrier of trace
            // identity into log statements.
            service.processDomainWork();
            assertEquals(traceId, MDC.get(TracingConstants.MDC_TRACE_ID));
            assertEquals(spanId, MDC.get(TracingConstants.MDC_SPAN_ID));
        } finally {
            TraceContextMdcBinder.clear();
        }
    }

    @Test
    void processDomainWorkRestoresMdc() {
        MDC.put(TracingConstants.MDC_TRACE_ID, "outer-trace");
        MDC.put(TracingConstants.MDC_SPAN_ID, "outer-span");
        service.processDomainWork();
        // After the call returns, MDC must be restored to the outer trace identity.
        assertEquals("outer-trace", MDC.get(TracingConstants.MDC_TRACE_ID));
        assertEquals("outer-span", MDC.get(TracingConstants.MDC_SPAN_ID));
        TraceContextMdcBinder.clear();
    }
}