package com.uber.doma.domain_billing.invoicing_ledger_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class InvoicingLedgerApplication {
    public static void main(String[] args) {
        SpringApplication.run(InvoicingLedgerApplication.class, args);
    }
}
