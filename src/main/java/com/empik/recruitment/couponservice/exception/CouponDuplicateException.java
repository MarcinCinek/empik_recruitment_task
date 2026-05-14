package com.empik.recruitment.couponservice.exception;

import lombok.Getter;

@Getter
public class CouponDuplicateException extends RuntimeException {

  private final String code;

  public CouponDuplicateException(String code) {
    this.code = code;
  }
}
