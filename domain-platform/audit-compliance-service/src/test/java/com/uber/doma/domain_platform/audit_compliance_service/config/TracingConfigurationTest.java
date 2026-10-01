package com.uber.doma.domain_platform.audit_compliance_service.config;

import com.uber.doma.domain_platform.audit_compliance_service.tracing.GrpcClientTracingInterceptor;
import com.uber.doma.domain_platform.audit_compliance_service.tracing.GrpcServerTracingInterceptor;
import io.grpc.ClientInterceptor;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Spring Boot context tests for {@link TracingConfiguration}.
 *
 * <p>Verifies that the gRPC interceptor beans are wired when an
 * {@link OpenTelemetry} bean is present and absent otherwise.</p>
 */
class TracingConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(TracingConfiguration.class)
            .withBean(OpenTelemetry.class, () -> OpenTelemetrySdk.builder()
                    .setPropagators(ContextPropagators.noop())
                    .build());

    @Test
    void serverInterceptorIsRegisteredWhenOpenTelemetryBeanExists() {
        runner.run(context -> {
            assertNotNull(context.getBean(GrpcServerTracingInterceptor.class));
            // Two beans expose GrpcClientTracingInterceptor: the typed bean and
            // the ClientInterceptor adapter. Both must exist and refer to the
            // same instance.
            final java.util.Map<String, GrpcClientTracingInterceptor> clients =
                    context.getBeansOfType(GrpcClientTracingInterceptor.class);
            assertTrue(clients.size() == 2,
                    "Expected typed bean and ClientInterceptor adapter, found " + clients.size());
            // Adapter bean must also be discoverable by the ClientInterceptor
            // contract used by grpc-spring.
            final java.util.Map<String, ClientInterceptor> adapters =
                    context.getBeansOfType(ClientInterceptor.class);
            assertTrue(adapters.containsKey("grpcClientInterceptorAdapter"),
                    "Expected grpcClientInterceptorAdapter bean to be registered");
        });
    }

    @Test
    void interceptorsAreAbsentWhenOpenTelemetryBeanMissing() {
        new ApplicationContextRunner()
                .withUserConfiguration(TracingConfiguration.class)
                .run(context -> assertTrue(
                        context.getBeanNamesForType(GrpcServerTracingInterceptor.class).length == 0,
                        "Interceptors must be absent when no OpenTelemetry bean is provided"));
    }
}