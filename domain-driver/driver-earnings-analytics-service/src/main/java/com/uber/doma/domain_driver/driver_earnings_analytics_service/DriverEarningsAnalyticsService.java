package com.uber.doma.domain_driver.driver_earnings_analytics_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DriverEarningsAnalyticsService {
    private static final Logger log = LoggerFactory.getLogger(DriverEarningsAnalyticsService.class);

    public void processDomainWork() {
        log.info("[DriverEarningsAnalytics] Executing bounded context domain logic for Driver Earnings Analytics");
    }
}
