package com.tripping.backend.insight.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 대시보드 상단 KPI 카드. 전부 실제 DB 집계값이며, 기간·지역 필터 기준은
// 트렌드 화면의 "총 방문 핑"과 완전히 동일하다(actual_route.travel_date 기준).
//
// ⚠️ 시간 기반 지표(체류 시간 등)는 여기 없다 — actual_route_spot.visit_time /
// widget_ping.ping_time은 "사용자가 원할 때 직접 찍는" 값이라 도착/출발 같은 의미를
// 부여할 수 없다. 좌표는 찍힌 시점의 실제 위치라서 신뢰할 수 있지만 시각은 아니다.
// 그래서 체류 시간 대신 시간에 의존하지 않는 "여행당 평균 방문 관광지 수"를 쓴다.
@Getter
@AllArgsConstructor
public class DashboardSummaryResponse {

    private long totalVisits;        // 총 방문 핑 - 스팟 체크인(actual_route_spot) 건수
    private double changeRate;       // 직전 동일 길이 구간 대비 증감률(%)

    private long activeTravelers;    // 활성 여행객 - 기간 내 여행 기록을 남긴 서로 다른 사용자 수
    private double travelerChangeRate;

    private long routeCount;         // 기록된 루트 - 기간 내 실제 여행(actual_route) 건수
    private double routeChangeRate;

    private double avgSpotsPerRoute; // 여행당 평균 방문 관광지 수 = totalVisits / routeCount
    private double avgSpotsChangeRate;

    private Double averageRating;    // 평균 만족도 - ping_log 평점 평균. 평점이 하나도 없으면 null
    private long ratingCount;        // 평균 계산에 쓰인 평점 개수 (신뢰도 표시용)
}
