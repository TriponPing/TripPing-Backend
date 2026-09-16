package com.tripping.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tourist_spot")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TouristSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "spot_id")
    private Long spotId;

    @Column(name = "api_content_id", length = 50)
    private String apiContentId; // 한국관광공사 OpenAPI content id

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String category;

    @Column(name = "region_id", length = 3)
    private String regionId; // 지역 필터링용 (TR-05)

    @Column(length = 255)
    private String address;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    // 👈 수정: @Lob 제거 - PostgreSQL + Hibernate 조합에서 String에 @Lob을 붙이면 text가 아니라
    // oid(Large Object 참조)로 매핑되는데, 이 프로젝트는 Supabase pgbouncer를 트랜잭션 풀링
    // 모드(포트 6543)로 붙어있어서 oid(Large Object)가 아예 동작 안 함 - "대형 객체는 자동 커밋
    // 모드에서 사용할 수 없습니다" 에러로 루트 추천/생성이 통째로 막히던 원인이 이거였음
    // (AppUser.profileImage에 적힌 것과 같은 종류의 버그, 이번엔 여기서도 터짐).
    // 컬럼명을 description_text로 새로 바꿈 - 기존 description 컬럼이 이미 oid 타입으로 굳어있어서
    // ddl-auto=update의 ALTER로는 안전하게 text로 못 되돌림(AppUser 쪽 주석과 동일한 이유).
    // 새 컬럼명을 쓰면 Hibernate가 위험한 ALTER 대신 깨끗한 CREATE로 처음부터 text로 만들어줌.
    // 예전 description(oid) 컬럼은 더 이상 안 쓰고 버려둠 - 지금까지 이 버그 때문에 정상적으로
    // 저장된 설명이 사실상 없어서(있어도 oid 숫자뿐) 버려도 데이터 손실 없음.
    @Column(name = "description_text", columnDefinition = "text")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Column(name = "created_by_user_id")
    private Long createdByUserId; // 이 장소를 등록한 유저. NULL이면 등록자 없음(공공데이터 등)
}