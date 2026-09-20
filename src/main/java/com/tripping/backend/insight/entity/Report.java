package com.tripping.backend.insight.entity;

import com.tripping.backend.entity.Organization;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// "보고서 센터"(TripPing-Web pages/Reports.tsx)에 뜨는, 실제로 저장된 보고서 1건.
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
public class Report {

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
    private String type; // "월간 트렌드" / "루트 네트워크" / "관광상품 기획안" (프론트 select 값 그대로)

    @Column(nullable = false, length = 30)
    private String period; // 집계에 쓴 기간. 트렌드 화면과 동일한 값("최근 7일"/"최근 30일"/"최근 1년")

    @Column(nullable = false, length = 30)
    private String region; // 집계에 쓴 지역 ("전체 지역" 이면 필터 없음, 트렌드 화면과 동일한 값)

    // 실제 보고서 본문 텍스트. 생성 시점의 트렌드 API 결과(총 방문 핑, 인기/급상승 루트)를
    // 문장으로 굳혀서 저장한다 — 프론트 lib/dashboardData.ts의 buildReportText()와 같은 발상.
    @Lob
    @Column(nullable = false)
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
