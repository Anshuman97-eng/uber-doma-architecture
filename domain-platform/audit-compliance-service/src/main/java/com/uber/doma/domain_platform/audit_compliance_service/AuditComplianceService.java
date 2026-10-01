package com.uber.doma.domain_platform.audit_compliance_service;

import com.uber.doma.domain_platform.audit_compliance_service.tracing.TraceContextMdcBinder;
import com.uber.doma.domain_platform.audit_compliance_service.tracing.TracingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
public class AuditComplianceService {
    private static final Logger log = LoggerFactory.getLogger(AuditComplianceService.class);

    public void processDomainWork() {
        final TraceContextMdcBinder.MdcSnapshot mdc = TraceContextMdcBinder.attachCurrent();
        try {
            final String traceId = MDC.get(TracingConstants.MDC_TRACE_ID);
            final String spanId = MDC.get(TracingConstants.MDC_SPAN_ID);
            log.info("[AuditCompliance] Executing bounded context domain logic for Immutable Audit Log and GDPR"
                            + " traceId={} spanId={}",
                    traceId, spanId);
        } finally {
            TraceContextMdcBinder.detach(mdc);
        }
    }
}
