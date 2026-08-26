package com.tripping.backend.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_user")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(name = "profile_image", length = 255)
    private String profileImage;

    @Column(name = "region_id", length = 3)
    private String regionId; // 거주 지역 (TR-01, TR-03)

    @Builder.Default
    @Column(length = 20)
    private String language = "ko";

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserLevel level = UserLevel.새끼; // TR-31 레벨 시스템

    @Builder.Default
    @Column(name = "is_resident_pinger")
    private Boolean isResidentPinger = false; // TR-12.1 주민핑거 여부

    // 빌더 패턴으로 객체를 생성할 때 값을 주지 않아도 기본값으로 "ROLE_USER"가 들어가도록 설정
    @Builder.Default
    @Column(nullable = false, length = 30) // DB 컬럼 설정: NULL 허용 안 함, 최대 길이 30
    private String role = "ROLE_USER"; // 회원의 권한 (예: 일반 유저는 ROLE_USER, 관리자는 ROLE_ADMIN)

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