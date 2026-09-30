package com.uber.doma.domain_billing.payment_orchestrator_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class PaymentOrchestratorApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentOrchestratorApplication.class, args);
    }
}
