package com.uber.doma.domain_platform.audit_compliance_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class AuditComplianceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuditComplianceApplication.class, args);
    }
}
