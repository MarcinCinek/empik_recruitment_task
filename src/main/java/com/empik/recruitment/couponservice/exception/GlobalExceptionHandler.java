package com.empik.recruitment.couponservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CouponNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleCouponNotFound() {
        return new ErrorResponse(
                Instant.now(),
                404,
                "Coupon not found"
        );
    }

    @ExceptionHandler(CouponAlreadyUsedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleAlreadyUsed() {
        return new ErrorResponse(
                Instant.now(),
                409,
                "Coupon already used"
        );
    }

    @ExceptionHandler(CouponLimitReachedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleLimitReached() {
        return new ErrorResponse(
                Instant.now(),
                409,
                "Coupon usage limit reached"
        );
    }

    @ExceptionHandler(InvalidCountryException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse handleInvalidCountry() {
        return new ErrorResponse(
                Instant.now(),
                422,
                "Coupon cannot be used in your country"
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleArgumentValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .toList();

        return new ErrorResponse(
                Instant.now(),
                400,
                "Validation argument failed: " + errors
        );
    }
}
