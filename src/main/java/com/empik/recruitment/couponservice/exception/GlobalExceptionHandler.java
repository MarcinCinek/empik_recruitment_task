package com.empik.recruitment.couponservice.exception;

import com.empik.recruitment.couponservice.metrics.CouponMetrics;
import com.empik.recruitment.couponservice.metrics.FailureReason;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@AllArgsConstructor
public class GlobalExceptionHandler {

  private final CouponMetrics metrics;

  @ExceptionHandler(CouponNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorResponse handleCouponNotFound(CouponNotFoundException ex) {
    log.error("Coupon not found", ex);
    metrics.incrementFailed(FailureReason.COUPON_NOT_FOUND.key());

    return new ErrorResponse(Instant.now(), 404, "Coupon not found");
  }

  @ExceptionHandler(CouponAlreadyUsedException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ErrorResponse handleAlreadyUsed(CouponAlreadyUsedException ex) {
    log.warn("Coupon already used", ex);
    metrics.incrementFailed(FailureReason.ALREADY_USED.key());

    return new ErrorResponse(Instant.now(), 409, "Coupon already used");
  }

  @ExceptionHandler(CouponLimitReachedException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ErrorResponse handleLimitReached(CouponLimitReachedException ex) {
    log.warn("Coupon limit reached", ex);
    metrics.incrementFailed(FailureReason.LIMIT_REACHED.key());

    return new ErrorResponse(Instant.now(), 409, "Coupon usage limit reached");
  }

  @ExceptionHandler(InvalidCountryException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  public ErrorResponse handleInvalidCountry(InvalidCountryException ex) {
    log.warn("Invalid country", ex);
    metrics.incrementFailed(FailureReason.INVALID_COUNTRY.key());

    return new ErrorResponse(Instant.now(), 422, "Coupon cannot be used in your country");
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleArgumentValidation(MethodArgumentNotValidException ex) {
    List<String> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
            .toList();

    log.warn("Validation failed: {}", errors);
    metrics.incrementFailed(FailureReason.VALIDATION_ERROR.key());

    return new ErrorResponse(Instant.now(), 400, "Validation argument failed: " + errors);
  }

  @ExceptionHandler(RuntimeException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorResponse handleRuntime(RuntimeException ex) {
    log.error("Unexpected error", ex);
    metrics.incrementFailed(FailureReason.INTERNAL_ERROR.key());

    return new ErrorResponse(
        Instant.now(), 500, "Internal error: " + ex.getClass().getSimpleName());
  }
}
