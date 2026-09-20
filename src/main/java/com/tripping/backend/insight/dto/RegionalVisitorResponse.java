package com.tripping.backend.insight.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class RegionalVisitorResponse {
    private LocalDate date;        // 날짜
    private long totalVisitors;    // 한국관광공사 방문자수 GW(DataLabService) 기준, 해당 시도 전체 방문자수(현지인+외지인+외국인 합산)
}
