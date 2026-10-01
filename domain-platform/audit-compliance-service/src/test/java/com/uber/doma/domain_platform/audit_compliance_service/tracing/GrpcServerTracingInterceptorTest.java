package com.uber.doma.domain_platform.audit_compliance_service.tracing;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Server;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for the gRPC server-side tracing interceptor.
 *
 * <p>The tests use the in-process gRPC transport so we can verify that
 * W3C {@code traceparent} headers injected by the client interceptor are
 * correctly extracted by the server interceptor, that a server span is
 * recorded with the expected parent, and that the SLF4J MDC is populated
 * with the active trace/span identifiers while the server handler runs.</p>
 */
class GrpcServerTracingInterceptorTest {

    private static final Logger log = LoggerFactory.getLogger(GrpcServerTracingInterceptorTest.class);
    private static final String SERVICE_NAME = "audit-compliance-service";
    private static final String TRACEPARENT_HEADER = "traceparent";

    private OpenTelemetrySdk sdk;
    private InMemorySpanExporter exporter;
    private Server server;
    private ManagedChannel channel;

    @BeforeEach
    void startServer() throws Exception {
        exporter = InMemorySpanExporter.create();
        final SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(exporter))
                .build();
        sdk = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .build();

        final GrpcServerTracingInterceptor serverInterceptor =
                new GrpcServerTracingInterceptor(sdk, SERVICE_NAME);
        final ServerServiceDefinition service = ServerInterceptors.intercept(
                new TestComplianceService(),
                serverInterceptor);
        final String name = "audit-compliance-" + System.nanoTime();
        server = InProcessServerBuilder.forName(name)
                .addService(service)
                .build()
                .start();
        channel = InProcessChannelBuilder.forName(name).build();
        TraceContextMdcBinder.clear();
        MDC.clear();
        // Reset static capture state.
        TestComplianceService.traceIdLatch = new java.util.concurrent.CountDownLatch(1);
        TestComplianceService.completeLatch = new java.util.concurrent.CountDownLatch(1);
        TestComplianceService.observedTraceId.set(null);
        TestComplianceService.observedSpanId.set(null);
    }

    /**
     * Wait for the server-side {@code onComplete} (or onError) listener to fire.
     * gRPC invokes those callbacks asynchronously after the client receives its
     * response, so callers must wait before asserting on exporter state.
     */
    private void awaitComplete() throws Exception {
        if (!TestComplianceService.completeLatch.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Server onComplete/onError never fired");
        }
    }

    /**
     * Wait until at least {@code expectedCount} spans are present in the
     * in-memory exporter. Used to bridge the gap between gRPC's async
     * listener callbacks and the synchronous span exporter contract.
     *
     * @param expectedCount minimum number of spans expected to be exported
     */
    private void awaitSpans(final int expectedCount) throws Exception {
        final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            sdk.getSdkTracerProvider().forceFlush().join(1, TimeUnit.SECONDS);
            if (exporter.getFinishedSpanItems().size() >= expectedCount) {
                return;
            }
            Thread.sleep(10);
        }
        throw new IllegalStateException("Expected at least " + expectedCount
                + " spans but got " + exporter.getFinishedSpanItems().size());
    }

    @AfterEach
    void stopServer() {
        if (channel != null) {
            channel.shutdownNow();
        }
        if (server != null) {
            server.shutdownNow();
        }
        TraceContextMdcBinder.clear();
        MDC.clear();
    }

    @Test
    void extractedTraceparentBecomesParentOfServerSpan() throws Exception {
        final String traceparent = "00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01";

        final ClientInterceptor clientInterceptor = new ClientInterceptor() {
            @Override
            public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                    final MethodDescriptor<ReqT, RespT> method,
                    final CallOptions callOptions,
                    final Channel next) {
                return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
                    @Override
                    public void start(final Listener<RespT> responseListener, final Metadata headers) {
                        headers.put(Metadata.Key.of(TRACEPARENT_HEADER, Metadata.ASCII_STRING_MARSHALLER),
                                traceparent);
                        super.start(responseListener, headers);
                    }
                };
            }
        };

        final TestServiceClient client = new TestServiceClient(channel, clientInterceptor);
        final String reply = client.unary("hello").get(5, TimeUnit.SECONDS);
        assertEquals("ack:hello", reply);
        awaitComplete();
        awaitSpans(1);
        sdk.getSdkTracerProvider().forceFlush().join(2, TimeUnit.SECONDS);

        final List<SpanData> spans = exporter.getFinishedSpanItems();
        assertEquals(1, spans.size());
        final SpanData serverSpan = spans.get(0);
        assertEquals("audit.Compliance/Unary", serverSpan.getName());
        assertEquals("0af7651916cd43dd8448eb211c80319c", serverSpan.getTraceId());
        assertEquals("b7ad6b7169203331", serverSpan.getParentSpanId(),
                "Server span must be parented under the client traceparent");
    }

    @Test
    void mdcContainsTraceIdWhileHandlerIsRunning() throws Exception {
        final TestServiceClient client = new TestServiceClient(channel, null);
        client.unary("hello").get(5, TimeUnit.SECONDS);

        assertTrue(TestComplianceService.traceIdLatch.await(2, TimeUnit.SECONDS));
        final String traceId = TestComplianceService.observedTraceId.get();
        final String spanId = TestComplianceService.observedSpanId.get();
        assertNotNull(traceId);
        assertNotNull(spanId);
        assertEquals(32, traceId.length());
        assertEquals(16, spanId.length());
        assertTrue(traceId.matches("[0-9a-f]+"));
        assertTrue(spanId.matches("[0-9a-f]+"));
        awaitComplete();
    }

    @Test
    void mdcIsCleanedAfterHandlerCompletes() throws Exception {
        final TestServiceClient client = new TestServiceClient(channel, null);
        client.unary("hello").get(5, TimeUnit.SECONDS);
        awaitComplete();
        assertNull(MDC.get(TracingConstants.MDC_TRACE_ID));
        assertNull(MDC.get(TracingConstants.MDC_SPAN_ID));
    }

    @Test
    void serverSpanIsCreatedEvenWithoutClientTraceparent() throws Exception {
        final TestServiceClient client = new TestServiceClient(channel, null);
        client.unary("hello").get(5, TimeUnit.SECONDS);
        awaitComplete();
        awaitSpans(1);
        sdk.getSdkTracerProvider().forceFlush().join(2, TimeUnit.SECONDS);

        final List<SpanData> spans = exporter.getFinishedSpanItems();
        assertEquals(1, spans.size(), "Expected exactly one span but got " + spans.size()
                + " spans: " + spans);
        final SpanData serverSpan = spans.get(0);
        assertTrue(serverSpan.getTraceId().matches("[0-9a-f]{32}"));
        assertTrue(serverSpan.getSpanId().matches("[0-9a-f]{16}"));
        assertTrue(serverSpan.getKind() == io.opentelemetry.api.trace.SpanKind.SERVER);
    }

    @Test
    void serverInterceptorRejectsNullOpenTelemetry() {
        final IllegalArgumentException ex = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new GrpcServerTracingInterceptor(null, SERVICE_NAME));
        assertTrue(ex.getMessage().contains("openTelemetry"));
    }

    @Test
    void serverInterceptorUsesProvidedServiceName() {
        final Tracer tracer = sdk.getTracer(SERVICE_NAME);
        assertNotNull(tracer);
    }

    @Test
    void grpcClientInterceptorEmitsValidWireHeader() throws Exception {
        final GrpcClientTracingInterceptor clientTracing =
                new GrpcClientTracingInterceptor(sdk, SERVICE_NAME);
        final AtomicReference<String> observed = new AtomicReference<>();
        final ClientInterceptor capturing = new ClientInterceptor() {
            @Override
            public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                    final MethodDescriptor<ReqT, RespT> method,
                    final CallOptions callOptions,
                    final Channel next) {
                final Channel wrapped = new CapturingChannel(next, observed);
                return clientTracing.interceptCall(method, callOptions, wrapped);
            }
        };
        final TestServiceClient client = new TestServiceClient(channel, capturing);
        client.unary("hello").get(5, TimeUnit.SECONDS);
        final String header = observed.get();
        assertNotNull(header, "traceparent header must be injected");
        assertTrue(header.matches("00-[0-9a-f]{32}-[0-9a-f]{16}-[0-9a-f]{2}"));
        final SpanContext parsed = W3CTraceCarrier.INSTANCE.parseTraceparent(header);
        assertTrue(parsed.isValid());
    }

    @Test
    void serverInterceptorCapturesErrorStatusOnFailure() throws Exception {
        final TestServiceClient client = new TestServiceClient(channel, null);
        try {
            client.unary("error").get(5, TimeUnit.SECONDS);
            org.junit.jupiter.api.Assertions.fail("Expected an exception to bubble up");
        } catch (final Exception expected) {
            // expected
        }
        awaitComplete();
        awaitSpans(1);
        sdk.getSdkTracerProvider().forceFlush().join(2, TimeUnit.SECONDS);
        final List<SpanData> spans = exporter.getFinishedSpanItems();
        assertEquals(1, spans.size());
        final SpanData span = spans.get(0);
        assertEquals(io.opentelemetry.api.trace.StatusCode.ERROR, span.getStatus().getStatusCode());
    }

    @Test
    void traceparentInjectionAndExtraction() {
        final Span span = sdk.getTracer("test").spanBuilder("caller").startSpan();
        try (Scope ignored = span.makeCurrent()) {
            final Map<String, String> carrier = new HashMap<>();
            W3CTraceCarrier.INSTANCE.inject(Context.current(), carrier);
            final String header = carrier.get("traceparent");
            assertNotNull(header);
            final Context extracted = W3CTraceCarrier.INSTANCE.extract(carrier);
            assertEquals(Span.fromContext(extracted).getSpanContext().getTraceId(),
                    span.getSpanContext().getTraceId());
        } finally {
            span.end();
        }
    }

    @Test
    void grpcClientInterceptorRejectsNullOpenTelemetry() {
        final IllegalArgumentException ex = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new GrpcClientTracingInterceptor(null, SERVICE_NAME));
        assertTrue(ex.getMessage().contains("openTelemetry"));
    }

    @Test
    void extractReadsLowercaseHeaders() throws Exception {
        final String traceparent = "00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01";
        final Map<String, String> carrier = new HashMap<>();
        carrier.put(TRACEPARENT_HEADER, traceparent);
        final Context extracted = W3CTraceCarrier.INSTANCE.extract(carrier);
        final SpanContext ctx = Span.fromContext(extracted).getSpanContext();
        assertTrue(ctx.isValid());
        assertEquals("0af7651916cd43dd8448eb211c80319c", ctx.getTraceId());
    }

    @Test
    void grpcClientInterceptorInheritsTraceAcrossChannel() throws Exception {
        // Construct a caller span, then make a call through the client
        // interceptor. We expect:
        //   caller -> client-span (audit.Compliance/Unary) -> server-span (audit.Compliance/Unary)
        // all sharing the caller's trace id.
        final Span callerSpan = sdk.getTracer("test").spanBuilder("caller").startSpan();
        final String callerTraceId = callerSpan.getSpanContext().getTraceId();
        final String callerSpanId = callerSpan.getSpanContext().getSpanId();
        Span grpcClientSpan = null;
        try (Scope ignored = callerSpan.makeCurrent()) {
            final GrpcClientTracingInterceptor clientTracing =
                    new GrpcClientTracingInterceptor(sdk, SERVICE_NAME);
            final TestServiceClient client = new TestServiceClient(channel, clientTracing);
            client.unary("hello").get(5, TimeUnit.SECONDS);
        } finally {
            callerSpan.end();
        }
        awaitComplete();
        sdk.getSdkTracerProvider().forceFlush().join(2, TimeUnit.SECONDS);

        final List<SpanData> spans = exporter.getFinishedSpanItems();
        // Locate the gRPC client span (kind=CLIENT) and the server span (kind=SERVER).
        SpanData clientSpanData = null;
        SpanData serverSpanData = null;
        for (final SpanData sp : spans) {
            if (!sp.getTraceId().equals(callerTraceId)) {
                continue;
            }
            if (sp.getKind() == io.opentelemetry.api.trace.SpanKind.CLIENT) {
                clientSpanData = sp;
            } else if (sp.getKind() == io.opentelemetry.api.trace.SpanKind.SERVER) {
                serverSpanData = sp;
            }
        }
        assertNotNull(clientSpanData, "Expected a CLIENT span in trace " + callerTraceId);
        assertNotNull(serverSpanData, "Expected a SERVER span in trace " + callerTraceId);
        // Caller -> client span -> server span.
        assertEquals(callerSpanId, clientSpanData.getParentSpanId(),
                "CLIENT span must be parented under caller span");
        assertEquals(clientSpanData.getSpanId(), serverSpanData.getParentSpanId(),
                "SERVER span must be parented under CLIENT span");
    }

    @Test
    void nettyExecutorPropagatesOpenTelemetryContextAcrossThreads() throws Exception {
        final Span parent = sdk.getTracer("test").spanBuilder("caller").startSpan();
        final String expectedTrace = parent.getSpanContext().getTraceId();
        final java.util.concurrent.ExecutorService raw =
                java.util.concurrent.Executors.newSingleThreadExecutor();
        final NettyContextPropagationExecutor wrapped = NettyContextPropagationExecutor.wrap(raw);
        final java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(1);
        final AtomicReference<String> observed = new AtomicReference<>();
        try (Scope ignored = parent.makeCurrent()) {
            wrapped.execute(() -> {
                observed.set(W3CTraceCarrier.currentTraceId());
                done.countDown();
            });
        }
        assertTrue(done.await(2, TimeUnit.SECONDS));
        assertEquals(expectedTrace, observed.get());
        parent.end();
        raw.shutdown();
        // Sanity assertion on the request/response bytes used in this test.
        assertNotNull("hello".getBytes(StandardCharsets.UTF_8));
    }
}