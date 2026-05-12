package com.empik.recruitment.couponservice.serviceImpl;

import com.empik.recruitment.couponservice.dto.CouponResponse;
import com.empik.recruitment.couponservice.dto.CreateCouponRequest;
import com.empik.recruitment.couponservice.dto.UseCouponRequest;
import com.empik.recruitment.couponservice.dto.UseCouponResponse;
import com.empik.recruitment.couponservice.entity.Coupon;
import com.empik.recruitment.couponservice.entity.CouponUsage;
import com.empik.recruitment.couponservice.exception.CouponAlreadyUsedException;
import com.empik.recruitment.couponservice.exception.CouponLimitReachedException;
import com.empik.recruitment.couponservice.exception.CouponNotFoundException;
import com.empik.recruitment.couponservice.exception.InvalidCountryException;
import com.empik.recruitment.couponservice.factory.CouponFactory;
import com.empik.recruitment.couponservice.geoip.GeoIpService;
import com.empik.recruitment.couponservice.mapper.CouponMapper;
import com.empik.recruitment.couponservice.metrics.CouponMetrics;
import com.empik.recruitment.couponservice.repository.CouponRepository;
import com.empik.recruitment.couponservice.repository.CouponUsageRepository;
import com.empik.recruitment.couponservice.service.CouponService;
import com.empik.recruitment.couponservice.util.CouponCodeNormalizer;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;

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
        metrics.incrementCreated();

        Coupon coupon = couponFactory.create(request);
        Coupon saved = couponRepository.save(coupon);

        return couponMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UseCouponResponse useCoupon(UseCouponRequest request, String ipAddress) {
        try {
            Coupon coupon = getCouponOrThrow(request);

            validateUseConstraints(coupon, request.userId(), ipAddress);

            saveUsage(coupon, request.userId());

            incrementUsageOrThrow(coupon);

            String country = geoIpService.resolveCountry(ipAddress);
            metrics.incrementUsed(country);

            return successResponse();

        } catch (RuntimeException ex) {
            metrics.incrementFailed(ex.getClass().getSimpleName());
            throw ex;
        }
    }

    private Coupon getCouponOrThrow(UseCouponRequest request) {
        String normalizedCode = CouponCodeNormalizer.normalize(request.couponCode());

        return couponRepository.findByCodeNormalized(normalizedCode)
                .orElseThrow(CouponNotFoundException::new);
    }

    private void validateUseConstraints(Coupon coupon, String userId, String ipAddress) {
        validateCountry(coupon, ipAddress);
        validateNotAlreadyUsed(coupon, userId);
    }

    private void validateCountry(Coupon coupon, String ipAddress) {
        String requestCountry = geoIpService.resolveCountry(ipAddress);

        if (!coupon.getCountryCode().equalsIgnoreCase(requestCountry)) {
            throw new InvalidCountryException();
        }
    }

    private void validateNotAlreadyUsed(Coupon coupon, String userId) {
        boolean alreadyUsed = couponUsageRepository.existsByCouponIdAndUserId(
                coupon.getId(),
                userId
        );

        if (alreadyUsed) {
            throw new CouponAlreadyUsedException();
        }
    }

    private void saveUsage(Coupon coupon, String userId) {
        try {
            couponUsageRepository.save(
                    CouponUsage.builder()
                            .coupon(coupon)
                            .userId(userId)
                            .usedAt(Instant.now())
                            .build()
            );
        } catch (DataIntegrityViolationException e) {
            throw new CouponAlreadyUsedException();
        }
    }

    private void incrementUsageOrThrow(Coupon coupon) {
        int updatedRows = couponRepository.incrementUsage(coupon.getId());

        if (updatedRows == 0) {
            throw new CouponLimitReachedException();
        }
    }

    private UseCouponResponse successResponse() {
        return new UseCouponResponse(true, "Coupon successfully used");
    }
}