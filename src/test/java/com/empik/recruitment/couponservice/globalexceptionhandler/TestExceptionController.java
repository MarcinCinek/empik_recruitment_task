package com.empik.recruitment.couponservice.globalexceptionhandler;

import com.empik.recruitment.couponservice.dto.UseCouponRequest;
import com.empik.recruitment.couponservice.service.CouponService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
class TestExceptionController {

  private final CouponService couponService;

  TestExceptionController(CouponService couponService) {
    this.couponService = couponService;
  }

  @PostMapping("/use")
  public Object use() {
    return couponService.useCoupon(new UseCouponRequest("TEST", "user"), "127.0.0.1");
  }

  @PostMapping("/validate")
  public void validate(@RequestBody @Valid TestRequest request) {}

  static class TestRequest {

    @NotBlank(message = "name must not be blank")
    public String name;
  }
}
