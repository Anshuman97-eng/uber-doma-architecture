package com.uber.doma.domain_platform.audit_compliance_service.tracing;

import io.grpc.MethodDescriptor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Test-only {@link MethodDescriptor} for the in-process tracing integration
 * tests. The descriptor marshals plain {@code byte[]} payloads so the tests
 * can stay free of generated protobuf code.
 */
final class TestDescriptors {

    /**
     * Marshaller that reads and writes plain {@code byte[]} payloads.
     */
    static final MethodDescriptor.Marshaller<byte[]> BYTE_MARSHALLER = new MethodDescriptor.Marshaller<>() {
        @Override
        public InputStream stream(final byte[] value) {
            return value == null ? new ByteArrayInputStream(new byte[0]) : new ByteArrayInputStream(value);
        }

        @Override
        public byte[] parse(final InputStream stream) {
            if (stream == null) {
                return new byte[0];
            }
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final byte[] buf = new byte[256];
            try {
                int read;
                while ((read = stream.read(buf)) != -1) {
                    out.write(buf, 0, read);
                }
            } catch (final IOException ex) {
                throw new RuntimeException("Failed to read byte[] stream", ex);
            }
            return out.toByteArray();
        }
    };

    /**
     * Shared {@link MethodDescriptor} for the integration suite.
     */
    static final MethodDescriptor<byte[], byte[]> METHOD = MethodDescriptor.<byte[], byte[]>newBuilder()
            .setType(MethodDescriptor.MethodType.UNARY)
            .setFullMethodName("audit.Compliance/Unary")
            .setRequestMarshaller(BYTE_MARSHALLER)
            .setResponseMarshaller(BYTE_MARSHALLER)
            .build();

    private TestDescriptors() {
    }
}