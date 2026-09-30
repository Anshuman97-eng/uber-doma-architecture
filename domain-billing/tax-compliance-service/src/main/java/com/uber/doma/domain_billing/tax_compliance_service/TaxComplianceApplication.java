package com.uber.doma.domain_billing.tax_compliance_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class TaxComplianceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaxComplianceApplication.class, args);
    }
}
