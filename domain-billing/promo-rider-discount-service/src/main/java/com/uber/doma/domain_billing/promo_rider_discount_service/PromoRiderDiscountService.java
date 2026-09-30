package com.uber.doma.domain_billing.promo_rider_discount_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PromoRiderDiscountService {
    private static final Logger log = LoggerFactory.getLogger(PromoRiderDiscountService.class);

    public void processDomainWork() {
        log.info("[PromoRiderDiscount] Executing bounded context domain logic for Ride Promo Coupons");
    }
}
