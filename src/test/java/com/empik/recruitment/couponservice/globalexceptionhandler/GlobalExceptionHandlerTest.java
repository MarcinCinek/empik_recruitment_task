package com.empik.recruitment.couponservice.globalexceptionhandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.empik.recruitment.couponservice.exception.*;
import com.empik.recruitment.couponservice.metrics.CouponMetrics;
import com.empik.recruitment.couponservice.metrics.FailureReason;
import com.empik.recruitment.couponservice.service.CouponService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

  private MockMvc mockMvc;

  private CouponService couponService = mock(CouponService.class);
  private CouponMetrics metrics = mock(CouponMetrics.class);

  @BeforeEach
  void setup() {
    GlobalExceptionHandler handler = new GlobalExceptionHandler(metrics);

    mockMvc =
        MockMvcBuilders.standaloneSetup(new TestExceptionController(couponService))
            .setControllerAdvice(handler)
            .build();
  }

  @Test
  void shouldReturn400_whenValidationFails() throws Exception {

    mockMvc
        .perform(
            post("/test/validate")
                .contentType("application/json")
                .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.message")
                .value(org.hamcrest.Matchers.containsString("name must not be blank")));

    verify(metrics).incrementFailed(FailureReason.VALIDATION_ERROR.key());
  }

  @Test
  void shouldReturn404_whenCouponNotFound() throws Exception {

    when(couponService.useCoupon(any(), anyString())).thenThrow(new CouponNotFoundException());

    mockMvc
        .perform(post("/test/use"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Coupon not found"));

    verify(metrics).incrementFailed(FailureReason.COUPON_NOT_FOUND.key());
  }

  @Test
  void shouldReturn409_whenAlreadyUsed() throws Exception {

    when(couponService.useCoupon(any(), anyString())).thenThrow(new CouponAlreadyUsedException());

    mockMvc
        .perform(post("/test/use"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("Coupon already used"));

    verify(metrics).incrementFailed(FailureReason.ALREADY_USED.key());
  }

  @Test
  void shouldReturn409_whenLimitReached() throws Exception {

    when(couponService.useCoupon(any(), anyString())).thenThrow(new CouponLimitReachedException());

    mockMvc
        .perform(post("/test/use"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("Coupon usage limit reached"));

    verify(metrics).incrementFailed(FailureReason.LIMIT_REACHED.key());
  }

  @Test
  void shouldReturn422_whenInvalidCountry() throws Exception {

    when(couponService.useCoupon(any(), anyString())).thenThrow(new InvalidCountryException());

    mockMvc
        .perform(post("/test/use"))
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.message").value("Coupon cannot be used in your country"));

    verify(metrics).incrementFailed(FailureReason.INVALID_COUNTRY.key());
  }

  @Test
  void shouldReturn500_whenRuntimeException() throws Exception {

    when(couponService.useCoupon(any(), anyString())).thenThrow(new RuntimeException("boom"));

    mockMvc
        .perform(post("/test/use"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.message").value("Internal error: RuntimeException"));

    verify(metrics).incrementFailed(FailureReason.INTERNAL_ERROR.key());
  }
}
