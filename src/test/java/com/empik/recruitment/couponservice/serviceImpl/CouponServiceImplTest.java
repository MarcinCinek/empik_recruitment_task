package com.empik.recruitment.couponservice.serviceImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.empik.recruitment.couponservice.dto.UseCouponRequest;
import com.empik.recruitment.couponservice.entity.Coupon;
import com.empik.recruitment.couponservice.exception.*;
import com.empik.recruitment.couponservice.geoip.GeoIpService;
import com.empik.recruitment.couponservice.repository.CouponRepository;
import com.empik.recruitment.couponservice.repository.CouponUsageRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CouponServiceImplTest {

  @InjectMocks private CouponServiceImpl service;

  @Mock private CouponRepository couponRepository;
  @Mock private CouponUsageRepository couponUsageRepository;
  @Mock private GeoIpService geoIpService;

  private Coupon coupon;

  @BeforeEach
  void setUp() {
    coupon =
        Coupon.builder()
            .id(UUID.randomUUID())
            .codeNormalized("CODE")
            .maxUsage(2)
            .usageCount(0)
            .countryCode("PL")
            .build();
  }

  @Test
  void shouldUseCouponSuccessfully() {
    when(couponRepository.findByCodeNormalized(any())).thenReturn(Optional.of(coupon));

    when(geoIpService.resolveCountry(any())).thenReturn("PL");

    when(couponUsageRepository.existsByCouponIdAndUserId(any(), any())).thenReturn(false);

    when(couponRepository.incrementUsage(any())).thenReturn(1);

    var result = service.useCoupon(new UseCouponRequest("CODE", "user1"), "127.0.0.1");

    assertTrue(result.success());
  }

  @Test
  void shouldThrowWhenCouponNotFound() {
    when(couponRepository.findByCodeNormalized(any())).thenReturn(Optional.empty());

    assertThrows(
        CouponNotFoundException.class,
        () -> service.useCoupon(new UseCouponRequest("CODE", "user1"), "127.0.0.1"));
  }

  @Test
  void shouldThrowWhenCountryInvalid() {
    when(couponRepository.findByCodeNormalized(any())).thenReturn(Optional.of(coupon));

    when(geoIpService.resolveCountry(any())).thenReturn("DE");

    assertThrows(
        InvalidCountryException.class,
        () -> service.useCoupon(new UseCouponRequest("CODE", "user1"), "1.1.1.1"));
  }

  @Test
  void shouldThrowWhenAlreadyUsed() {
    when(couponRepository.findByCodeNormalized(any())).thenReturn(Optional.of(coupon));

    when(geoIpService.resolveCountry(any())).thenReturn("PL");

    when(couponUsageRepository.existsByCouponIdAndUserId(any(), any())).thenReturn(true);

    assertThrows(
        CouponAlreadyUsedException.class,
        () -> service.useCoupon(new UseCouponRequest("CODE", "user1"), "127.0.0.1"));
  }

  @Test
  void shouldThrowWhenLimitReached() {
    coupon.setUsageCount(2);

    when(couponRepository.findByCodeNormalized(any())).thenReturn(Optional.of(coupon));

    when(geoIpService.resolveCountry(any())).thenReturn("PL");

    when(couponUsageRepository.existsByCouponIdAndUserId(any(), any())).thenReturn(false);

    when(couponRepository.incrementUsage(any())).thenReturn(0);

    assertThrows(
        CouponLimitReachedException.class,
        () -> service.useCoupon(new UseCouponRequest("CODE", "user1"), "127.0.0.1"));
  }

  @Test
  void shouldNormalizeCodeCaseInsensitive() {
    // given
    when(couponRepository.findByCodeNormalized("WIOSNA")).thenReturn(Optional.of(coupon));

    when(geoIpService.resolveCountry(any())).thenReturn("PL");

    when(couponUsageRepository.existsByCouponIdAndUserId(any(), any())).thenReturn(false);

    when(couponRepository.incrementUsage(any())).thenReturn(1);

    // when
    var result = service.useCoupon(new UseCouponRequest("wIoSnA", "user1"), "127.0.0.1");

    // then
    assertTrue(result.success());

    verify(couponRepository).findByCodeNormalized("WIOSNA");
  }
}
