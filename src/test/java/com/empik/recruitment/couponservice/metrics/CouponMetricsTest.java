package com.empik.recruitment.couponservice.metrics;

import static org.junit.jupiter.api.Assertions.*;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class CouponMetricsTest {

  @Test
  void shouldIncrementFailedCounter() {
    // given
    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    CouponMetrics metrics = new CouponMetrics(registry);

    // when
    metrics.incrementFailed("VALIDATION_ERROR");
    metrics.incrementFailed("VALIDATION_ERROR");

    // then
    double count =
        Objects.requireNonNull(
                registry.find("coupon.use.failed").tag("reason", "VALIDATION_ERROR").counter())
            .count();

    assertEquals(2.0, count);
  }

  @Test
  void shouldCreateSeparateCountersForDifferentReasons() {
    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    CouponMetrics metrics = new CouponMetrics(registry);

    metrics.incrementFailed("A");
    metrics.incrementFailed("B");

    assertEquals(
        1.0,
        Objects.requireNonNull(registry.find("coupon.use.failed").tag("reason", "A").counter())
            .count());

    assertEquals(
        1.0,
        Objects.requireNonNull(registry.find("coupon.use.failed").tag("reason", "B").counter())
            .count());
  }
}
