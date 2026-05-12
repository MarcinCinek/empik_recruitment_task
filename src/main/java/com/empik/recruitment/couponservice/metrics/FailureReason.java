package com.empik.recruitment.couponservice.metrics;

public enum FailureReason {

    INVALID_COUNTRY("invalid_country"),
    LIMIT_REACHED("limit_reached"),
    ALREADY_USED("already_used"),
    VALIDATION_ERROR("validation_error"),
    COUPON_NOT_FOUND("coupon_not_found"),
    INTERNAL_ERROR("internal_error");

    private final String key;

    FailureReason(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}