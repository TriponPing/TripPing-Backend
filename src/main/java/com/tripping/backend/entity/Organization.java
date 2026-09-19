package com.tripping.backend.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "organization")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "org_id")
    private Long orgId;

    @Column(name = "org_name", nullable = false, length = 100)
    private String orgName;

    @Column(name = "org_type", nullable = false, length = 30)
    private String orgType; // 여행사/지자체

    @Column(name = "manager_name", nullable = false, length = 50)
    private String managerName;

    @Column(name = "manager_email", nullable = false, length = 100)
    private String managerEmail;

    @Column(nullable = false, length = 255)
    private String password;

    // 👈 추가: 접근 신청 시 제출하는 재직/기관 증빙파일 URL. AppUser.profileImage 때와 같은
    // base64 컬럼 버그(oid 문제, 주석 참고)를 피하려고 base64로 직접 저장하지 않고
    // FileUploadController로 먼저 업로드해서 받은 URL 문자열만 저장한다.
    @Column(name = "document_url", length = 500)
    private String documentUrl;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrgStatus status = OrgStatus.PENDING;

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
