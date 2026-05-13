package com.empik.recruitment.couponservice.util;

import java.util.Locale;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class CouponCodeNormalizer {

  public static String normalize(String code) {
    return code.toUpperCase(Locale.ROOT);
  }
}
