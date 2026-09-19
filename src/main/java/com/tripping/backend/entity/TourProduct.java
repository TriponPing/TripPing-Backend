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

    // 👈 추가: 기획 화면의 상품 소개 글. trendBasis(트렌드 근거)와 용도가 달라
    // 따로 둔다. 길이도 255로는 부족하다.
    @Column(length = 1000)
    private String description;

    // 👈 추가: 판매가(원). 회의록의 "상세 설정 (가격·기간·타겟)" 항목이다.
    // 아직 정하지 않은 초안이 많아 null을 허용한다.
    @Column
    private Integer price;

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