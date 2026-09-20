package com.tripping.backend.insight.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

// POST /b2b/insight/reports 요청 바디. 프론트 "새 보고서 만들기" 모달 입력값 그대로 받는다.
@Getter
@NoArgsConstructor
public class ReportCreateRequest {
    private String title;  // 보고서 제목 (예: "9월 제주 관광 트렌드 분석")
    private String type;   // "월간 트렌드" / "루트 네트워크" / "관광상품 기획안"
    private String period; // "최근 7일" / "최근 30일" / "최근 1년" — 트렌드 화면과 동일한 값을 그대로 씀
    private String region; // "전체 지역" 등 — 트렌드 화면과 동일한 값
}
