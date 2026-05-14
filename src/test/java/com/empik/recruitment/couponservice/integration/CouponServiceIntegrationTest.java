package com.empik.recruitment.couponservice.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.empik.recruitment.couponservice.dto.CreateCouponRequest;
import com.empik.recruitment.couponservice.dto.UseCouponRequest;
import com.empik.recruitment.couponservice.entity.Coupon;
import com.empik.recruitment.couponservice.exception.CouponAlreadyUsedException;
import com.empik.recruitment.couponservice.exception.CouponLimitReachedException;
import com.empik.recruitment.couponservice.exception.CouponNotFoundException;
import com.empik.recruitment.couponservice.exception.InvalidCountryException;
import com.empik.recruitment.couponservice.geoip.GeoIpService;
import com.empik.recruitment.couponservice.repository.CouponRepository;
import com.empik.recruitment.couponservice.repository.CouponUsageRepository;
import com.empik.recruitment.couponservice.service.CouponService;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class CouponServiceIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("coupondb")
          .withUsername("test")
          .withPassword("test")
          .withReuse(false);

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    if (!postgres.isRunning()) {
      postgres.start();
    }

    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);

    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
  }

  @Autowired private CouponService couponService;

  @Autowired private CouponRepository couponRepository;

  @Autowired private CouponUsageRepository couponUsageRepository;

  @MockitoBean private GeoIpService geoIpService;

  @BeforeEach
  void setup() {
    couponUsageRepository.deleteAll();
    couponRepository.deleteAll();
  }

  @Test
  void shouldCreateCouponSuccessfully() {

    CreateCouponRequest request = new CreateCouponRequest("WIOSNA", 10, "PL");

    var response = couponService.createCoupon(request);

    assertNotNull(response);
    assertEquals("WIOSNA", response.code());

    Coupon saved = couponRepository.findByCodeNormalized("WIOSNA").orElseThrow();

    assertEquals("PL", saved.getCountryCode());
    assertEquals(10, saved.getMaxUsage());
    assertEquals(0, saved.getUsageCount());
  }

  @Test
  void shouldTreatCouponCodeAsCaseInsensitive() {

    Coupon coupon =
        couponRepository.saveAndFlush(
            Coupon.builder()
                .code("WIOSNA")
                .codeNormalized("WIOSNA")
                .createdAt(Instant.now())
                .maxUsage(5)
                .usageCount(0)
                .countryCode("PL")
                .build());

    when(geoIpService.resolveCountry(anyString())).thenReturn("PL");

    var response = couponService.useCoupon(new UseCouponRequest("wIoSnA", "user-1"), "127.0.0.1");

    assertTrue(response.success());

    Coupon updated = couponRepository.findById(coupon.getId()).orElseThrow();

    assertEquals(1, updated.getUsageCount());
  }

  @Test
  void shouldThrowWhenCouponDoesNotExist() {

    when(geoIpService.resolveCountry(anyString())).thenReturn("PL");

    assertThrows(
        CouponNotFoundException.class,
        () -> couponService.useCoupon(new UseCouponRequest("NOT_EXIST", "user-1"), "127.0.0.1"));
  }

  @Test
  void shouldThrowWhenCountryIsInvalid() {

    couponRepository.saveAndFlush(
        Coupon.builder()
            .code("PLONLY")
            .codeNormalized("PLONLY")
            .createdAt(Instant.now())
            .maxUsage(10)
            .usageCount(0)
            .countryCode("PL")
            .build());

    when(geoIpService.resolveCountry(anyString())).thenReturn("DE");

    assertThrows(
        InvalidCountryException.class,
        () -> couponService.useCoupon(new UseCouponRequest("PLONLY", "user-1"), "8.8.8.8"));
  }

  @Test
  void shouldNotAllowUserToUseCouponTwice() {

    couponRepository.saveAndFlush(
        Coupon.builder()
            .code("ONCE")
            .codeNormalized("ONCE")
            .createdAt(Instant.now())
            .maxUsage(10)
            .usageCount(0)
            .countryCode("PL")
            .build());

    when(geoIpService.resolveCountry(anyString())).thenReturn("PL");

    couponService.useCoupon(new UseCouponRequest("ONCE", "user-1"), "127.0.0.1");

    assertThrows(
        CouponAlreadyUsedException.class,
        () -> couponService.useCoupon(new UseCouponRequest("ONCE", "user-1"), "127.0.0.1"));
  }

  @Test
  void shouldStopUsingCouponWhenLimitReached() {

    couponRepository.saveAndFlush(
        Coupon.builder()
            .code("LIMIT")
            .codeNormalized("LIMIT")
            .createdAt(Instant.now())
            .maxUsage(1)
            .usageCount(0)
            .countryCode("PL")
            .build());

    when(geoIpService.resolveCountry(anyString())).thenReturn("PL");

    couponService.useCoupon(new UseCouponRequest("LIMIT", "user-1"), "127.0.0.1");

    assertThrows(
        CouponLimitReachedException.class,
        () -> couponService.useCoupon(new UseCouponRequest("LIMIT", "user-2"), "127.0.0.1"));
  }

  @Test
  void shouldEnforceUniqueNormalizedCodeConstraint() {

    couponRepository.saveAndFlush(
        Coupon.builder()
            .code("WIOSNA")
            .codeNormalized("WIOSNA")
            .createdAt(Instant.now())
            .maxUsage(10)
            .usageCount(0)
            .countryCode("PL")
            .build());

    assertThrows(
        DataIntegrityViolationException.class,
        () ->
            couponRepository.saveAndFlush(
                Coupon.builder()
                    .code("wiosna")
                    .codeNormalized("WIOSNA")
                    .createdAt(Instant.now())
                    .maxUsage(10)
                    .usageCount(0)
                    .countryCode("PL")
                    .build()));
  }

  @Test
  void shouldAllowOnlyMaxUsage_whenMultipleUsersUseCouponSimultaneously() throws Exception {

    couponRepository.saveAndFlush(
        Coupon.builder()
            .code("CONCURRENT")
            .codeNormalized("CONCURRENT")
            .createdAt(Instant.now())
            .maxUsage(3)
            .usageCount(0)
            .countryCode("PL")
            .build());

    when(geoIpService.resolveCountry(anyString())).thenReturn("PL");

    int users = 10;

    ExecutorService executor = Executors.newFixedThreadPool(users);

    CountDownLatch ready = new CountDownLatch(users);

    CountDownLatch start = new CountDownLatch(1);

    List<Future<Boolean>> results =
        IntStream.range(0, users)
            .mapToObj(id -> executor.submit(createCouponTask(id, ready, start)))
            .toList();

    ready.await();

    start.countDown();

    long successCount = countSuccessfulResults(results);

    Coupon updated = couponRepository.findByCodeNormalized("CONCURRENT").orElseThrow();

    assertEquals(3, successCount);
    assertEquals(3, updated.getUsageCount());

    executor.shutdown();
  }

  private java.util.concurrent.Callable<Boolean> createCouponTask(
      int userId, CountDownLatch ready, CountDownLatch start) {

    return () -> {
      ready.countDown();
      start.await();

      try {
        couponService.useCoupon(new UseCouponRequest("CONCURRENT", "user-" + userId), "127.0.0.1");

        return true;

      } catch (CouponLimitReachedException e) {
        return false;
      }
    };
  }

  private long countSuccessfulResults(List<Future<Boolean>> results) {

    return results.stream()
        .map(
            future -> {
              try {
                return future.get();
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            })
        .filter(Boolean::booleanValue)
        .count();
  }
}
