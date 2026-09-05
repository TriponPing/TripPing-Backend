package com.tripping.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// 유저별 뱃지 "꺼내기(노출)" 여부. 뱃지 자체의 획득 조건/카탈로그는 아직 없어서
// mypage.service.BadgeCatalog에 고정 목록으로 정의해두고, 이 테이블은 그중 뭘 프로필에
// 노출할지(featured)만 유저별로 저장함. 유저가 한 번도 건드린 적 없으면 행 자체가 없고,
// 이 경우 서비스 레이어에서 전부 featured=true로 최초 1회 초기화함.
@Entity
@Table(name = "user_badge_setting", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "badge_code"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UserBadgeSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_badge_setting_id")
    private Long userBadgeSettingId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "badge_code", nullable = false, length = 30)
    private String badgeCode;

    @Column(nullable = false)
    private Boolean featured;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
