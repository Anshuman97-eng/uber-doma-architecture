package com.uber.doma.domain_platform.event_streaming_hub_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.uber.doma")
public class EventStreamingHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventStreamingHubApplication.class, args);
    }
}
