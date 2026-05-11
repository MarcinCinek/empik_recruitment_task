package com.empik.recruitment.couponservice.factory;

import com.empik.recruitment.couponservice.dto.CreateCouponRequest;
import com.empik.recruitment.couponservice.entity.Coupon;
import com.empik.recruitment.couponservice.util.CouponCodeNormalizer;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Locale;

@Component
public class CouponFactory {

    public Coupon create(CreateCouponRequest request) {
        return Coupon.builder()
                .code(request.code())
                .codeNormalized(CouponCodeNormalizer.normalize(request.code()))
                .createdAt(Instant.now())
                .maxUsage(request.maxUsage())
                .usageCount(0)
                .countryCode(request.countryCode().toUpperCase(Locale.ROOT))
                .build();
    }
}
