package com.uber.doma.domain_platform.audit_compliance_service.config;

import com.uber.doma.domain_platform.audit_compliance_service.tracing.GrpcClientTracingInterceptor;
import com.uber.doma.domain_platform.audit_compliance_service.tracing.GrpcServerTracingInterceptor;
import io.grpc.ClientInterceptor;
import io.opentelemetry.api.OpenTelemetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that wires the W3C Trace Context propagation
 * components into the gRPC client and server stacks.
 *
 * <p>The interceptor beans are only registered when the Spring Boot
 * Micrometer Tracing auto-configuration has produced an {@link OpenTelemetry}
 * bean; this keeps the configuration opt-in and safe to load in tests that
 * do not provide a tracer.</p>
 */
@Configuration
public class TracingConfiguration {

    /**
     * Default service name used when {@code spring.application.name} is unset.
     */
    public static final String DEFAULT_SERVICE_NAME = "audit-compliance-service";

    /**
     * Server interceptor that extracts W3C traceparent from incoming calls.
     *
     * @param openTelemetry the OpenTelemetry SDK to source the tracer from
     * @param serviceName   application service name
     * @return configured server interceptor
     */
    @Bean
    @ConditionalOnBean(OpenTelemetry.class)
    public GrpcServerTracingInterceptor grpcServerTracingInterceptor(
            final OpenTelemetry openTelemetry,
            @Value("${spring.application.name:" + DEFAULT_SERVICE_NAME + "}") final String serviceName) {
        return new GrpcServerTracingInterceptor(openTelemetry, serviceName);
    }

    /**
     * Client interceptor that injects W3C traceparent into outgoing calls.
     *
     * @param openTelemetry the OpenTelemetry SDK to source the tracer from
     * @param serviceName   application service name
     * @return configured client interceptor
     */
    @Bean
    @ConditionalOnBean(OpenTelemetry.class)
    public GrpcClientTracingInterceptor grpcClientTracingInterceptor(
            final OpenTelemetry openTelemetry,
            @Value("${spring.application.name:" + DEFAULT_SERVICE_NAME + "}") final String serviceName) {
        return new GrpcClientTracingInterceptor(openTelemetry, serviceName);
    }

    /**
     * Adapter bean so the typed client interceptor also implements the
     * generic {@link ClientInterceptor} interface required by grpc-spring.
     *
     * @param typedInterceptor the typed interceptor bean
     * @return a {@link ClientInterceptor} view of the same instance
     */
    @Bean
    @ConditionalOnBean(GrpcClientTracingInterceptor.class)
    public ClientInterceptor grpcClientInterceptorAdapter(final GrpcClientTracingInterceptor typedInterceptor) {
        return typedInterceptor;
    }
}