package com.tripping.backend.insight.entity;

import com.tripping.backend.entity.Organization;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// "보고서 센터"(TripPing-Web pages/Reports.tsx)에 뜨는, 실제로 저장된 보고서 1건.
//
// 클래스 이름을 InsightReport로 지은 이유: 원래 Report로 만들었더니 기존 앱에 이미 있던
// com.tripping.backend.entity.Report(콘텐츠 신고용 — 게시물/댓글 신고 처리 상태)랑
// 클래스 이름이 겹쳐서 Hibernate가 "엔티티 이름이 중복된다"고 에러를 냈다
// (패키지가 달라도 JPA 엔티티 이름은 기본적으로 클래스 이름 기준이라 이렇게 됨).
// 그래서 아예 이름 자체를 InsightReport로 바꿔서 안 겹치게 함. 테이블명(insight_report)은 그대로.
//
// 트렌드 화면(InsightController)은 항상 "지금 시점 기준"으로 실시간 재계산해서 보여주지만,
// 보고서는 다르다: 담당자가 "9월 제주 트렌드 분석" 같은 보고서를 만들면, 그 시점에 계산된
// 숫자를 텍스트로 굳혀서(content) 저장해둔다. 나중에 실제 방문 데이터가 더 쌓여서
// 트렌드 화면 숫자가 바뀌어도, 이미 만든 보고서 내용은 그대로 남아있어야 하기 때문
// (보고서 = 그 순간의 스냅샷, 트렌드 화면 = 실시간).
@Entity
@Table(name = "insight_report")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class InsightReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    // 어느 기관이 만든 보고서인지. 지금 버전은 "보고서 = 기관 담당자 전용 기능"으로 보고
    // Admin(운영자) 계정은 아예 못 만들게 컨트롤러 단에서 막아뒀다 — 그래서 not-null.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization organization;

    @Column(nullable = false, length = 100)
    private String title; // 예: "9월 제주 관광 트렌드 분석" (프론트 모달 입력값 그대로)

    @Column(nullable = false, length = 30)
    private String type; // "트렌드 분석" / "루트 네트워크" / "상품 기획안" (프론트 select 값 그대로)

    @Column(nullable = false, length = 30)
    private String period; // 집계에 쓴 기간. 트렌드 화면과 동일한 값("최근 7일"/"최근 30일"/"최근 1년")

    @Column(nullable = false, length = 30)
    private String region; // 집계에 쓴 지역 ("전체 지역" 이면 필터 없음, 트렌드 화면과 동일한 값)

    // 상품 기획안 유형일 때만 채워진다 - 어떤 tour_product를 기반으로 만든 보고서인지.
    // 트렌드/루트 네트워크 보고서는 특정 상품에 매이지 않으므로 null.
    // 다운로드 시점에 프론트가 이 id로 최신 상품 상세를 다시 조회해서 PDF를 만든다.
    @Column(name = "product_id")
    private Long productId;

    // 실제 보고서 본문 텍스트. 생성 시점의 트렌드 API 결과(총 방문 핑, 인기/급상승 루트)를
    // 문장으로 굳혀서 저장한다 — 프론트 lib/dashboardData.ts의 buildReportText()와 같은 발상.
    // @Lob이었으나 PostgreSQL에서 large object(oid)로 매핑되어 자동커밋 모드에서 조회 시
    // "대형 객체는 자동 커밋 모드에서 사용할 수 없습니다" 에러가 남 — AppUser.profileImage 때와
    // 같은 문제라 그때처럼 columnDefinition="text"로 일반 텍스트 컬럼으로 매핑되게 바꿈.
    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReportStatus status = ReportStatus.COMPLETED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
