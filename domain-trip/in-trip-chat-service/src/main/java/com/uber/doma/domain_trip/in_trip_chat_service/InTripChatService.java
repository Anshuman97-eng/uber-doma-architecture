package com.uber.doma.domain_trip.in_trip_chat_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InTripChatService {
    private static final Logger log = LoggerFactory.getLogger(InTripChatService.class);

    public void processDomainWork() {
        log.info("[InTripChat] Executing bounded context domain logic for Masked Chat & VoIP Call");
    }
}
