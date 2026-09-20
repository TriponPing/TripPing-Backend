package com.tripping.backend.insight.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RouteRankingResponse {
    private String routeName;   // 예: "성산일출봉 → 섭지코지 → 우도"
    private long visitCount;    // 선택한 기간 내 이 루트 조합의 방문 횟수
    private double changeRate;  // 직전 동일 길이 기간 대비 증감률(%)
}
