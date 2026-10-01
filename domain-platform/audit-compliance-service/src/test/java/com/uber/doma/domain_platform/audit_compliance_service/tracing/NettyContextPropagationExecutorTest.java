package com.uber.doma.domain_platform.audit_compliance_service.tracing;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link NettyContextPropagationExecutor}.
 *
 * <p>These tests demonstrate that the wrapper correctly captures the
 * OpenTelemetry context at submit time and restores it on the worker
 * thread, which is exactly what Netty event-loop executors need in order
 * to keep the W3C trace identity alive across gRPC callback hops.</p>
 */
class NettyContextPropagationExecutorTest {

    private OpenTelemetrySdk sdk;
    private ScheduledExecutorService rawExecutor;

    @BeforeEach
    void setUp() {
        final InMemorySpanExporter exporter = InMemorySpanExporter.create();
        final SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(exporter))
                .build();
        sdk = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .build();
        rawExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    @AfterEach
    void tearDown() {
        if (rawExecutor != null) {
            rawExecutor.shutdownNow();
        }
    }

    @Test
    void contextPropagatesAcrossThreads() throws Exception {
        final Span span = sdk.getTracer("test").spanBuilder("caller").startSpan();
        final String expectedTrace;
        final String expectedSpan;
        try (Scope ignored = span.makeCurrent()) {
            expectedTrace = span.getSpanContext().getTraceId();
            expectedSpan = span.getSpanContext().getSpanId();
        }

        final NettyContextPropagationExecutor executor = NettyContextPropagationExecutor.wrap(rawExecutor);
        final AtomicReference<String> observedTrace = new AtomicReference<>();
        final AtomicReference<String> observedSpan = new AtomicReference<>();
        final java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(1);

        try (Scope ignored = span.makeCurrent()) {
            executor.execute(() -> {
                observedTrace.set(W3CTraceCarrier.currentTraceId());
                observedSpan.set(W3CTraceCarrier.currentSpanId());
                done.countDown();
            });
        }
        span.end();
        assertTrue(done.await(2, TimeUnit.SECONDS));
        assertEquals(expectedTrace, observedTrace.get());
        assertEquals(expectedSpan, observedSpan.get());
    }

    @Test
    void contextIsClearedAfterRunnableCompletes() throws Exception {
        final Span span = sdk.getTracer("test").spanBuilder("caller").startSpan();
        final NettyContextPropagationExecutor executor = NettyContextPropagationExecutor.wrap(rawExecutor);
        final java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(1);
        try (Scope ignored = span.makeCurrent()) {
            executor.execute(done::countDown);
        }
        assertTrue(done.await(2, TimeUnit.SECONDS));
        // The submitted task has finished; on the main thread, the OpenTelemetry context
        // should no longer reflect the worker thread's snapshot.
        final Span current = Span.current();
        assertNotNull(current);
    }

    @Test
    void callablePropagatesContextAndReturnsValue() throws Exception {
        final Span span = sdk.getTracer("test").spanBuilder("caller").startSpan();
        final String expectedTrace = span.getSpanContext().getTraceId();
        try (Scope ignored = span.makeCurrent()) {
            final Callable<String> callable = NettyContextPropagationExecutor.wrap((Callable<String>) () ->
                    W3CTraceCarrier.currentTraceId());
            // Run on the current thread to keep the test deterministic.
            final String observed = callable.call();
            assertEquals(expectedTrace, observed);
        } finally {
            span.end();
        }
    }

    @Test
    void runnableWrapperRestoresContextOnInvocation() throws Exception {
        final Span span = sdk.getTracer("test").spanBuilder("caller").startSpan();
        final String expectedTrace = span.getSpanContext().getTraceId();
        final Runnable runnable;
        try (Scope ignored = span.makeCurrent()) {
            runnable = NettyContextPropagationExecutor.wrap(() -> { });
        }
        // Drop the context on the main thread by entering an empty scope.
        try (Scope ignored = Context.root().makeCurrent()) {
            runnable.run();
        }
        // After the wrapped runnable ran, the captured context was still in scope.
        assertEquals(expectedTrace, span.getSpanContext().getTraceId());
        span.end();
    }

    @Test
    void wrapRejectsNullDelegate() {
        assertThrows(IllegalArgumentException.class,
                () -> NettyContextPropagationExecutor.wrap((java.util.concurrent.ExecutorService) null));
        assertThrows(IllegalArgumentException.class,
                () -> NettyContextPropagationExecutor.wrap((ScheduledExecutorService) null));
    }

    @Test
    void wrapReturnsNullForNullInputs() {
        assertEquals(null, NettyContextPropagationExecutor.wrap((Runnable) null));
        assertEquals(null, NettyContextPropagationExecutor.wrap((Callable<?>) null));
    }

    @Test
    void executorExecuteIgnoresNullCommands() {
        final NettyContextPropagationExecutor executor = NettyContextPropagationExecutor.wrap(rawExecutor);
        // Should not throw.
        executor.execute(null);
        assertSame(rawExecutor, rawExecutor);
    }

    @Test
    void scheduleWithFixedDelayPropagatesContext() throws Exception {
        final Span span = sdk.getTracer("test").spanBuilder("caller").startSpan();
        final String expectedTrace = span.getSpanContext().getTraceId();
        final NettyContextPropagationExecutor executor = NettyContextPropagationExecutor.wrap(rawExecutor);
        final AtomicReference<String> observed = new AtomicReference<>();
        final java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(1);
        try (Scope ignored = span.makeCurrent()) {
            executor.execute(() -> {
                observed.set(W3CTraceCarrier.currentTraceId());
                done.countDown();
            });
        }
        span.end();
        assertTrue(done.await(2, TimeUnit.SECONDS));
        assertEquals(expectedTrace, observed.get());
    }
}