package com.uber.doma.domain_billing.fare_quotation_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class FareQuotationApplication {
    public static void main(String[] args) {
        SpringApplication.run(FareQuotationApplication.class, args);
    }
}
