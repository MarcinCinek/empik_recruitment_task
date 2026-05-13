package com.empik.recruitment.couponservice.mapper;

import com.empik.recruitment.couponservice.dto.CouponResponse;
import com.empik.recruitment.couponservice.entity.Coupon;
import org.springframework.stereotype.Component;

@Component
public class CouponMapper {

  public CouponResponse toResponse(Coupon coupon) {
    return new CouponResponse(
        coupon.getId(),
        coupon.getCode(),
        coupon.getCreatedAt(),
        coupon.getMaxUsage(),
        coupon.getUsageCount(),
        coupon.getCountryCode());
  }
}
