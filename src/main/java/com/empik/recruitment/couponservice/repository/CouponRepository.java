package com.empik.recruitment.couponservice.repository;

import com.empik.recruitment.couponservice.entity.Coupon;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {

    Optional<Coupon> findByCodeNormalized(String codeNormalized);

    @Modifying
    @Query("""
        UPDATE Coupon c
        SET c.usageCount = c.usageCount + 1
        WHERE c.id = :couponId
          AND c.usageCount < c.maxUsage
    """)
    int incrementUsage(@Param("couponId") UUID couponId);
}
