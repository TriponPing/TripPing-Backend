package com.tripping.backend.insight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class RegionalVisitorResponse {
    private LocalDate date;        // 날짜
    private long totalVisitors;    // 한국관광공사 방문자수(DataLabService) 기준, 7일 이동평균 평활 적용 후 최종값.
                                    // 실측 데이터가 없는 최근 구간은 작년 동기 데이터 × 보정계수로 추정한 값이다.
    @JsonProperty("isEstimated")
    private boolean isEstimated;   // true면 추정치(작년 동기 기반), false면 실측치.
                                    // 프론트에서 실측 구간은 실선, 추정 구간은 점선 등으로 구분해서 그리면 된다.
}
