package com.empik.recruitment.couponservice.controller;

import com.empik.recruitment.couponservice.dto.*;
import com.empik.recruitment.couponservice.service.CouponService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CouponResponse createCoupon(@Valid @RequestBody CreateCouponRequest request) {
        return couponService.createCoupon(request);
    }

    @PostMapping("/use")
    public UseCouponResponse useCoupon(
            @Valid @RequestBody UseCouponRequest request, HttpServletRequest httpServletRequest) {
        String ipAddress = httpServletRequest.getRemoteAddr();

        return couponService.useCoupon(request, ipAddress);
    }
}
