package com.tripping.backend.insight.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TrendsSummaryResponse {
    private long totalVisits;   // 선택한 기간(+지역)의 총 방문 핑 수
    private double changeRate;  // 직전 동일 길이 기간 대비 증감률(%). 직전 기간이 0이면 100.0(신규 취급)
}
