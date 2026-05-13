package com.empik.recruitment.couponservice.exception;

import java.time.Instant;

public record ErrorResponse(Instant timestamp, int status, String message) {}
