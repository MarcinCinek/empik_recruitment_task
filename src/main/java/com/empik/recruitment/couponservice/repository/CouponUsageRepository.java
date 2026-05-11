package com.empik.recruitment.couponservice.repository;

import com.empik.recruitment.couponservice.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, UUID> {

    boolean existsByCouponIdAndUserId(UUID couponId, String userId);
}
