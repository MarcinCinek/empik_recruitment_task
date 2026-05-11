package com.empik.recruitment.couponservice.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class CouponMetrics {

    private final Counter couponCreatedCounter;
    private final Counter couponUsedCounter;
    private final Counter couponUseFailedCounter;

    public CouponMetrics(MeterRegistry registry) {

        this.couponCreatedCounter = Counter.builder("coupon.created")
                .description("Number of created coupons")
                .register(registry);

        this.couponUsedCounter = Counter.builder("coupon.used")
                .description("Number of successfully used coupons")
                .register(registry);

        this.couponUseFailedCounter = Counter.builder("coupon.use.failed")
                .description("Number of failed coupon uses")
                .register(registry);
    }

    public void incrementCreated() {
        couponCreatedCounter.increment();
    }

    public void incrementUsed() {
        couponUsedCounter.increment();
    }

    public void incrementFailed() {
        couponUseFailedCounter.increment();
    }
}
