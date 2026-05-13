package com.empik.recruitment.couponservice.dto;

import java.time.Instant;
import java.util.UUID;

public record CouponResponse(
    UUID id,
    String code,
    Instant createdAt,
    Integer maxUsage,
    Integer usageCount,
    String countryCode) {}
