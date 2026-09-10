package com.tripping.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// 지역핑: 본인 거주 지역(AppUser.regionId)의 장소에 남기는 평점/후기.
// 장소 자체는 새 엔티티를 만들지 않고 기존 TouristSpot(장소 등록 흐름)을 그대로 재사용하고,
// 여기서는 "그 장소 + 이 유저의 평점/후기"만 저장함.
@Entity
@Table(name = "region_ping")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RegionPing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "region_ping_id")
    private Long regionPingId;

    @Column(name = "spot_id", nullable = false)
    private Long spotId; // TouristSpot 참조 (기존 장소 또는 새로 등록한 장소)

    @Column(name = "user_id", nullable = false)
    private Long userId; // 작성자

    @Column(nullable = false)
    private Integer rating;

    @Lob
    private String reviewComment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
