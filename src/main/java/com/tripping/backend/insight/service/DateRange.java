package com.tripping.backend.insight.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

// 프론트의 "최근 7일/최근 30일/최근 1년" 필터를 실제 날짜 구간으로 바꿔주고,
// 증감률 계산에 쓸 "바로 직전 동일 길이 구간"도 계산해준다.
public record DateRange(LocalDate start, LocalDate end) {

    public static DateRange forPeriod(String period, LocalDate today) {
        return switch (period) {
            case "최근 7일" -> new DateRange(today.minusDays(6), today);
            case "최근 1년" -> new DateRange(today.minusYears(1).plusDays(1), today);
            default -> new DateRange(today.minusDays(29), today); // "최근 30일" 및 알 수 없는 값의 기본값
        };
    }

    // 예: 이번 구간이 8/1~8/31(31일)이면, 직전 구간은 7/1~7/31(같은 길이)
    public DateRange previous() {
        long lengthInDays = ChronoUnit.DAYS.between(start, end) + 1;
        LocalDate previousEnd = start.minusDays(1);
        LocalDate previousStart = previousEnd.minusDays(lengthInDays - 1);
        return new DateRange(previousStart, previousEnd);
    }
}
