package com.empik.recruitment.couponservice.repository;

import com.empik.recruitment.couponservice.entity.CouponUsage;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, UUID> {

  boolean existsByCouponIdAndUserId(UUID couponId, String userId);
}
