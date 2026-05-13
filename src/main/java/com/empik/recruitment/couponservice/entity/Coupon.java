package com.empik.recruitment.couponservice.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(
        name = "coupons",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "code_normalized")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private String code;

  @Column(name = "code_normalized", nullable = false, updatable = false)
  private String codeNormalized;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Integer maxUsage;

  @Column(nullable = false)
  @Builder.Default
  private Integer usageCount = 0;

  @Column(nullable = false, length = 2)
  private String countryCode;
}
