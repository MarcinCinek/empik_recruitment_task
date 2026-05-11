package com.empik.recruitment.couponservice.util;

import lombok.NoArgsConstructor;

import java.util.Locale;

@NoArgsConstructor
public final class CouponCodeNormalizer {

    public static String normalize(String code) {
        return code.toUpperCase(Locale.ROOT);
    }
}
