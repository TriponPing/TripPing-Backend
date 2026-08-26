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