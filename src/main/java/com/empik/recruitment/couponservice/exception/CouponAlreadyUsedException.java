package com.empik.recruitment.couponservice.exception;

import lombok.Getter;

@Getter
public class CouponAlreadyUsedException extends RuntimeException {
  private final String userId;
  private final String couponCode;

  public CouponAlreadyUsedException(String userId, String couponCode) {

    this.userId = userId;
    this.couponCode = couponCode;
  }
}
