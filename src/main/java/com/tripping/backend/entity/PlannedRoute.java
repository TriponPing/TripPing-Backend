package com.tripping.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "planned_route")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PlannedRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "planned_route_id")
    private Long plannedRouteId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "candidate_id")
    private Long candidateId; // 추천 루트 기반이면 참조, 직접 생성이면 NULL

    @Column(length = 100)
    private String title;

    @Builder.Default
    @Column(name = "is_shared")
    private Boolean isShared = false;

    // 이 계획이 실제 여행으로 전환(TripService.createTrip)됐는지 여부.
    // "나의 여행 지도 > 내 계획" 탭에서 아직 시작 안 한 계획만 보여주기 위해 씀.
    // NOT NULL로 안 둔 이유: 기존에 이미 데이터가 쌓여있는 테이블이라, NOT NULL로 컬럼을
    // 추가하면 기존 행들 때문에 ALTER TABLE 자체가 실패함 - null은 "false(안 시작함)"로 취급.
    @Builder.Default
    @Column(name = "is_started")
    private Boolean isStarted = false;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false; // 소프트 삭제 여부

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