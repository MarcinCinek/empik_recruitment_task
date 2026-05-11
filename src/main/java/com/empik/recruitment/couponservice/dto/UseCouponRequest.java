package com.empik.recruitment.couponservice.dto;

import jakarta.validation.constraints.NotBlank;

public record UseCouponRequest(

        @NotBlank
        String couponCode,

        @NotBlank
        String userId
) {
}
