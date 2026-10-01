package com.uber.doma.domain_platform.audit_compliance_service.tracing;

/**
 * Centralized constants for W3C Distributed Tracing Context Propagation.
 *
 * <p>The values declared here correspond to the W3C Trace Context Recommendation
 * (https://www.w3.org/TR/trace-context/) and the W3C Baggage specification
 * (https://www.w3.org/TR/baggage/) which form the canonical contract for
 * propagating trace identity across process and thread boundaries.</p>
 */
public final class TracingConstants {

    private TracingConstants() {
    }

    /**
     * W3C standard header carrying the trace identity for the current request.
     * Format: {@code <version>-<trace-id>-<parent-id>-<trace-flags>}.
     */
    public static final String TRACEPARENT_HEADER = "traceparent";

    /**
     * Optional W3C header carrying vendor-specific trace data.
     */
    public static final String TRACESTATE_HEADER = "tracestate";

    /**
     * SLF4J MDC key for the current W3C trace identifier.
     */
    public static final String MDC_TRACE_ID = "traceId";

    /**
     * SLF4J MDC key for the current span identifier.
     */
    public static final String MDC_SPAN_ID = "spanId";

    /**
     * SLF4J MDC key for the parent span identifier (when applicable).
     */
    public static final String MDC_PARENT_SPAN_ID = "parentSpanId";

    /**
     * SLF4J MDC key for the service name emitting the log statement.
     */
    public static final String MDC_SERVICE_NAME = "serviceName";

    /**
     * Number of hex characters that make up a valid W3C trace identifier.
     */
    public static final int TRACE_ID_HEX_LENGTH = 32;

    /**
     * Number of hex characters that make up a valid W3C span identifier.
     */
    public static final int SPAN_ID_HEX_LENGTH = 16;
}
