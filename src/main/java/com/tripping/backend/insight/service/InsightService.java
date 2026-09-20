package com.tripping.backend.insight.service;

import com.tripping.backend.insight.dto.RouteRankingResponse;
import com.tripping.backend.insight.dto.TrendsSummaryResponse;
import com.tripping.backend.insight.repository.InsightRouteRepository;
import com.tripping.backend.insight.repository.RouteCountProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class InsightService {

    private final InsightRouteRepository insightRouteRepository;

    public TrendsSummaryResponse summary(String period, String region) {
        DateRange current = DateRange.forPeriod(period, LocalDate.now());
        DateRange previous = current.previous();
        String regionName = normalizeRegion(region);

        long currentTotal = nullToZero(insightRouteRepository.countVisits(current.start(), current.end(), regionName));
        long previousTotal = nullToZero(insightRouteRepository.countVisits(previous.start(), previous.end(), regionName));

        return new TrendsSummaryResponse(currentTotal, round1(percentChange(currentTotal, previousTotal)));
    }

    public List<RouteRankingResponse> risingRoutes(String period, String region) {
        DateRange current = DateRange.forPeriod(period, LocalDate.now());
        DateRange previous = current.previous();
        String regionName = normalizeRegion(region);

        List<RouteCountProjection> currentRoutes = insightRouteRepository.findTopRoutes(current.start(), current.end(), regionName);
        List<RouteCountProjection> previousRoutes = insightRouteRepository.findTopRoutes(previous.start(), previous.end(), regionName);

        // 루트 이름(방문 순서까지 포함한 문자열)을 키로 직전 기간 방문 횟수를 찾아서 증감률 계산.
        Map<String, Long> previousCounts = previousRoutes.stream()
                .collect(Collectors.toMap(RouteCountProjection::getRouteName, RouteCountProjection::getVisitCount, (a, b) -> a));

        return currentRoutes.stream()
                .map(row -> {
                    long previousCount = previousCounts.getOrDefault(row.getRouteName(), 0L);
                    double changeRate = round1(percentChange(row.getVisitCount(), previousCount));
                    return new RouteRankingResponse(row.getRouteName(), row.getVisitCount(), changeRate);
                })
                .sorted(Comparator.comparingLong(RouteRankingResponse::getVisitCount).reversed())
                .toList();
    }

    // 프론트의 "전체 지역"은 지역 필터 없음(null)으로 취급.
    private String normalizeRegion(String region) {
        if (region == null || region.isBlank() || region.equals("전체 지역")) return null;
        return region;
    }

    // 직전 기간이 0인데 이번 기간도 0이면 변화 없음(0%), 0에서 뭔가 생겼으면 "신규 급상승"
    // 의미로 +100%를 준다 (0으로 나누기를 피하면서도 "새로 생긴 루트"를 상위로 보이게 함).
    private double percentChange(long current, long previous) {
        if (previous == 0) return current == 0 ? 0.0 : 100.0;
        return ((double) (current - previous) / previous) * 100.0;
    }

    private double round1(double value) {
        return Math.round(value * 10) / 10.0;
    }

    private long nullToZero(Long value) {
        return value == null ? 0L : value;
    }
}
