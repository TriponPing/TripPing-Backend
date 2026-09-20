package com.tripping.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 기관이 외부 시스템에서 트립핑 데이터를 불러올 때 쓰는 키.
//
// 키 원문은 저장하지 않는다. 발급 직후 한 번만 보여주고 DB에는 해시만 남겨,
// DB가 새더라도 실제 키가 바로 노출되지 않게 한다. 목록 화면에서 어떤 키인지
// 알아볼 수 있도록 앞 8자(keyPrefix)만 따로 보관한다.
@Entity
@Table(name = "org_api_key")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrgApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "key_id")
    private Long keyId;

    @Column(name = "org_id", nullable = false)
    private Long orgId;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(name = "key_prefix", nullable = false, length = 16)
    private String keyPrefix;

    @Column(name = "key_hash", nullable = false, length = 255)
    private String keyHash;

    @Builder.Default
    @Column(nullable = false)
    private Boolean revoked = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
