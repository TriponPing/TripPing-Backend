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

    // 👈 수정: 255자 제한이었는데, 아직 별도 이미지 업로드/저장소가 없어서 프론트가 사진을
    // base64 데이터 URI로 인코딩해 그대로 저장함 - 255자로는 어림도 없어서 길이 제한을 없앰.
    // ⚠️ @Lob은 절대 쓰면 안 됨 - PostgreSQL + Hibernate 조합에서 String에 @Lob을 붙이면
    // text가 아니라 oid(Large Object 참조)로 매핑되는 경우가 있는데, 이 oid 값을 읽으려면
    // 저장할 때와 같은 트랜잭션이 열려있어야 해서, 로그인처럼 트랜잭션 밖(또는 다른 트랜잭션)에서
    // 유저 엔티티를 조회하면 그 즉시 예외가 나서 로그인 자체가 403으로 막혀버림 (실제로 겪은 버그).
    // 컬럼명을 profile_image_data로 새로 바꿈 - 예전 profile_image 컬럼이 이미 실제 DB에서
    // oid 타입으로 굳어버려서, ddl-auto=update의 ALTER로는 안전하게 text로 못 되돌림(시도해보니
    // 여전히 oid로 남아있어서 긴 문자열 저장 시 500 에러). 새 컬럼명을 쓰면 Hibernate가 위험한
    // ALTER 대신 깨끗한 CREATE로 처리해서 처음부터 text로 정상 생성됨. 예전 profile_image
    // 컬럼은 더 이상 안 쓰고 그냥 버려둠(안에 든 값도 이미 의미 없는 oid 숫자뿐이라 버려도 무방).
    @Column(name = "profile_image_data", columnDefinition = "text")
    private String profileImage;

    @Column(name = "region_id", length = 3)
    private String regionId; // 거주 지역 (TR-01, TR-03)

    @Builder.Default
    @Column(length = 20)
    private String language = "ko";

    // 👈 수정: 레벨을 AppUser에 저장해두던 걸 제거함 - 예전 레벨 이름(새끼/약지/중지/검지/엄지)이
    // 그대로 DB에 문자열로 박혀있어서, 10단계 새 이름(UserLevel 참고)으로 enum을 바꾸면
    // 기존 값이 새 enum 상수와 하나도 안 맞아 그 유저를 조회(로그인 포함)할 때마다
    // Enum.valueOf 예외가 터져 전부 로그인이 막히는 위험이 있었음(전에 겪었던 것과 같은 종류의
    // 버그). 대신 레벨은 저장하지 않고, 조회 시점의 총 핑 개수로 매번 새로 계산함
    // (MyPageProfileService 참고) - 핑 찍을 때마다 레벨업이 항상 실제 활동량과 정확히 일치함.

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