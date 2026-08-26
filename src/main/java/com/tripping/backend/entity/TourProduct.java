package com.tripping.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tour_product")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TourProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "org_id", nullable = false)
    private Long orgId;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "target_customer", length = 100)
    private String targetCustomer;

    @Column(name = "region_id", length = 3)
    private String regionId;

    @Column(name = "expected_duration")
    private Integer expectedDuration; // 분 단위

    @Column(length = 30)
    private String transport;

    @Builder.Default
    @Column(name = "meal_included")
    private Boolean mealIncluded = false;

    @Column(name = "trend_basis", length = 255)
    private String trendBasis;

    @Column(name = "caution_notes", length = 255)
    private String cautionNotes;

    @Builder.Default
    @Column(length = 20)
    private String status = "DRAFT";

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false; // 소프트 삭제 여부 - BS-17

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}