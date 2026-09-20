package com.tripping.backend.insight.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class DailyVisitResponse {
    private LocalDate date;    // 날짜 (actual_route.travel_date 기준 - summary/routes API와 동일한 필터)
    private long visitCount;   // 그 날 발생한 방문 핑(스팟 체크인) 수. 방문 기록이 없으면 0으로 채워서 내려간다.
}
