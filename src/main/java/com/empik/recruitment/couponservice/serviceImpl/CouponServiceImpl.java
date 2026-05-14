package com.empik.recruitment.couponservice.serviceImpl;

import com.empik.recruitment.couponservice.dto.CouponResponse;
import com.empik.recruitment.couponservice.dto.CreateCouponRequest;
import com.empik.recruitment.couponservice.dto.UseCouponRequest;
import com.empik.recruitment.couponservice.dto.UseCouponResponse;
import com.empik.recruitment.couponservice.entity.Coupon;
import com.empik.recruitment.couponservice.entity.CouponUsage;
import com.empik.recruitment.couponservice.exception.*;
import com.empik.recruitment.couponservice.factory.CouponFactory;
import com.empik.recruitment.couponservice.geoip.GeoIpService;
import com.empik.recruitment.couponservice.mapper.CouponMapper;
import com.empik.recruitment.couponservice.metrics.CouponMetrics;
import com.empik.recruitment.couponservice.repository.CouponRepository;
import com.empik.recruitment.couponservice.repository.CouponUsageRepository;
import com.empik.recruitment.couponservice.service.CouponService;
import com.empik.recruitment.couponservice.util.CouponCodeNormalizer;
import jakarta.transaction.Transactional;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

  private final CouponRepository couponRepository;
  private final CouponUsageRepository couponUsageRepository;
  private final GeoIpService geoIpService;

  private final CouponMapper couponMapper;
  private final CouponFactory couponFactory;

  private final CouponMetrics metrics;

  @Override
  public CouponResponse createCoupon(CreateCouponRequest request) {

    String normalized = CouponCodeNormalizer.normalize(request.code());

    if (couponRepository.existsByCodeNormalized(normalized)) {
      throw new CouponDuplicateException(normalized);
    }

    Coupon coupon = couponFactory.create(request);
    Coupon saved = couponRepository.save(coupon);

    metrics.incrementCreated();

    return couponMapper.toResponse(saved);
  }

  @Override
  @Transactional
  public UseCouponResponse useCoupon(UseCouponRequest request, String ipAddress) {
    String userId = request.userId();

    Coupon coupon = getCouponOrThrow(userId, request.couponCode());

    validateUseConstraints(coupon, userId, ipAddress);

    saveUsage(coupon, userId);

    incrementUsageOrThrow(coupon, userId);

    String country = geoIpService.resolveCountry(ipAddress);
    metrics.incrementUsed(country);

    return successResponse();
  }

  private Coupon getCouponOrThrow(String userId, String couponCode) {
    String normalizedCode = CouponCodeNormalizer.normalize(couponCode);

    return couponRepository
        .findByCodeNormalized(normalizedCode)
        .orElseThrow(() -> new CouponNotFoundException(userId, couponCode));
  }

  private void validateUseConstraints(Coupon coupon, String userId, String ipAddress) {
    validateCountry(coupon, ipAddress, userId);
    validateNotAlreadyUsed(coupon, userId);
  }

  private void validateCountry(Coupon coupon, String ipAddress, String userId) {
    String requestCountry = geoIpService.resolveCountry(ipAddress);

    if (!coupon.getCountryCode().equalsIgnoreCase(requestCountry)) {
      throw new InvalidCountryException(userId);
    }
  }

  private void validateNotAlreadyUsed(Coupon coupon, String userId) {
    boolean alreadyUsed = couponUsageRepository.existsByCouponIdAndUserId(coupon.getId(), userId);

    if (alreadyUsed) {
      throw new CouponAlreadyUsedException(userId, coupon.getCode());
    }
  }

  private void saveUsage(Coupon coupon, String userId) {
    try {
      couponUsageRepository.save(
          CouponUsage.builder().coupon(coupon).userId(userId).usedAt(Instant.now()).build());
    } catch (DataIntegrityViolationException e) {
      throw new CouponAlreadyUsedException(userId, coupon.getCode());
    }
  }

  private void incrementUsageOrThrow(Coupon coupon, String userId) {
    int updatedRows = couponRepository.incrementUsage(coupon.getId());

    if (updatedRows == 0) {
      throw new CouponLimitReachedException(userId, coupon.getCode());
    }
  }

  private UseCouponResponse successResponse() {
    return new UseCouponResponse(true, "Coupon successfully used");
  }
}
