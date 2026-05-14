package com.empik.recruitment.couponservice.exception;

import lombok.Getter;

@Getter
public class InvalidCountryException extends RuntimeException {
  private final String userId;

  public InvalidCountryException(String userId) {

    this.userId = userId;
  }
}
