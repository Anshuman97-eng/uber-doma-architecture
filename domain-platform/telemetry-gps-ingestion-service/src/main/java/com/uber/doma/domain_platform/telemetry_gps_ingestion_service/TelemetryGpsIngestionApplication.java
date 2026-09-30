package com.uber.doma.domain_platform.telemetry_gps_ingestion_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class TelemetryGpsIngestionApplication {
    public static void main(String[] args) {
        SpringApplication.run(TelemetryGpsIngestionApplication.class, args);
    }
}
