package com.empik.recruitment.couponservice.exception;

import lombok.Getter;

@Getter
public class CouponLimitReachedException extends RuntimeException {
  private final String userId;
  private final String couponCode;

  public CouponLimitReachedException(String userId, String couponCode) {

    this.userId = userId;
    this.couponCode = couponCode;
  }
}
