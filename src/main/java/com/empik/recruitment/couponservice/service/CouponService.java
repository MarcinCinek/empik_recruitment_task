package com.empik.recruitment.couponservice.service;

import com.empik.recruitment.couponservice.dto.*;

public interface CouponService {

  CouponResponse createCoupon(CreateCouponRequest request);

  UseCouponResponse useCoupon(UseCouponRequest request, String ipAddress);
}
