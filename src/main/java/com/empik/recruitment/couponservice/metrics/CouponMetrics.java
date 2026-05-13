package com.empik.recruitment.couponservice.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class CouponMetrics {

  private final MeterRegistry meterRegistry;
  private final Counter couponCreatedCounter;

  private final ConcurrentHashMap<String, Counter> usedByCountry = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Counter> failedByReason = new ConcurrentHashMap<>();

  public CouponMetrics(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;

    this.couponCreatedCounter =
        Counter.builder("coupon.created")
            .description("Number of created coupons")
            .register(meterRegistry);
  }

  public void incrementCreated() {
    couponCreatedCounter.increment();
  }

  public void incrementUsed(String country) {
    usedByCountry
        .computeIfAbsent(
            country,
            c ->
                Counter.builder("coupon.used")
                    .description("Number of successfully used coupons")
                    .tag("country", c)
                    .register(meterRegistry))
        .increment();
  }

  public void incrementFailed(String reason) {
    failedByReason
        .computeIfAbsent(
            reason,
            r ->
                Counter.builder("coupon.use.failed")
                    .description("Number of failed coupon uses")
                    .tag("reason", r)
                    .register(meterRegistry))
        .increment();
  }
}
