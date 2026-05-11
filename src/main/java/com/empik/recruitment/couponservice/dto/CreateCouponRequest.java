package com.empik.recruitment.couponservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateCouponRequest(

        @NotBlank
        String code,

        @NotNull
        @Positive
        Integer maxUsage,

        @NotBlank
        String countryCode
) {
}
