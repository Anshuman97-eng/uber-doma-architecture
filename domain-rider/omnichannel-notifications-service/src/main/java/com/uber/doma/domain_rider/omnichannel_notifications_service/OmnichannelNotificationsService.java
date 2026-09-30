package com.uber.doma.domain_rider.omnichannel_notifications_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OmnichannelNotificationsService {
    private static final Logger log = LoggerFactory.getLogger(OmnichannelNotificationsService.class);

    public void processDomainWork() {
        log.info("[OmnichannelNotifications] Executing bounded context domain logic for Push, SMS & Alert Notifications");
    }
}
