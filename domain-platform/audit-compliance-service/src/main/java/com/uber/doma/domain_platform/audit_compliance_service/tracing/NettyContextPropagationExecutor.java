package com.uber.doma.domain_platform.audit_compliance_service.tracing;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Wraps a Netty {@link Executor} so that tasks scheduled on it inherit the
 * OpenTelemetry {@link Context} (and therefore the W3C trace identity) that
 * was active on the submitting thread.
 *
 * <p>gRPC uses Netty event-loop executors for both inbound and outbound call
 * scheduling; without this wrapper the trace context would be lost the
 * moment a task crosses a thread boundary, breaking end-to-end trace
 * correlation.</p>
 */
public final class NettyContextPropagationExecutor implements Executor {

    private final Executor delegate;

    /**
     * @param delegate the underlying executor to wrap
     */
    public NettyContextPropagationExecutor(final Executor delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("delegate must not be null");
        }
        this.delegate = delegate;
    }

    @Override
    public void execute(final Runnable command) {
        if (command == null) {
            return;
        }
        final Context captured = Context.current();
        delegate.execute(() -> {
            try (Scope ignored = captured.makeCurrent()) {
                command.run();
            }
        });
    }

    /**
     * Convenience factory that wraps an existing {@link ExecutorService}.
     *
     * @param executor the executor service to wrap
     * @return a new {@link NettyContextPropagationExecutor} instance
     */
    public static NettyContextPropagationExecutor wrap(final ExecutorService executor) {
        return new NettyContextPropagationExecutor(executor);
    }

    /**
     * Convenience factory that wraps a {@link ScheduledExecutorService}.
     *
     * @param executor the executor service to wrap
     * @return a new {@link NettyContextPropagationExecutor} instance
     */
    public static NettyContextPropagationExecutor wrap(final ScheduledExecutorService executor) {
        return new NettyContextPropagationExecutor(executor);
    }

    /**
     * Wrap a {@link Callable} so that, when invoked, the captured OpenTelemetry
     * context from the submitting thread is restored before the call body runs.
     *
     * @param callable task to wrap
     * @param <T>      callable return type
     * @return a callable that establishes the captured scope before delegation
     */
    public static <T> Callable<T> wrap(final Callable<T> callable) {
        if (callable == null) {
            return null;
        }
        final Context captured = Context.current();
        return () -> {
            try (Scope ignored = captured.makeCurrent()) {
                return callable.call();
            }
        };
    }

    /**
     * Wrap a {@link Runnable} so that, when invoked, the captured OpenTelemetry
     * context from the submitting thread is restored before the task runs.
     *
     * @param runnable task to wrap
     * @return a runnable that establishes the captured scope before delegation
     */
    public static Runnable wrap(final Runnable runnable) {
        if (runnable == null) {
            return null;
        }
        final Context captured = Context.current();
        return () -> {
            try (Scope ignored = captured.makeCurrent()) {
                runnable.run();
            }
        };
    }
}