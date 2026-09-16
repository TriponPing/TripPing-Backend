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

    // 👈 수정: @Lob 제거 - TouristSpot.description과 완전히 같은 이유(그쪽 주석 참고)로
    // oid(Large Object)에 걸려 이 지역핑 후기를 읽거나 쓸 때마다 같은 에러가 날 상태였음.
    // 컬럼명도 같은 이유로 review_comment_text로 새로 바꿔서 Hibernate가 깨끗한 CREATE로
    // text 타입을 새로 만들게 함 (ALTER로는 oid에서 안전하게 못 돌아옴).
    @Column(name = "review_comment_text", columnDefinition = "text")
    private String reviewComment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
