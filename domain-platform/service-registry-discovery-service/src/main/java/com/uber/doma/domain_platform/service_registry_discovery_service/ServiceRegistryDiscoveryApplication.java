package com.uber.doma.domain_platform.service_registry_discovery_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class ServiceRegistryDiscoveryApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceRegistryDiscoveryApplication.class, args);
    }
}
