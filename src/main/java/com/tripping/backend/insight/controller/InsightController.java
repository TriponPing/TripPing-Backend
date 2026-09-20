package com.tripping.backend.insight.controller;

import com.tripping.backend.insight.dto.DailyVisitResponse;
import com.tripping.backend.insight.dto.RegionalVisitorResponse;
import com.tripping.backend.insight.dto.RouteRankingResponse;
import com.tripping.backend.insight.dto.TrendsSummaryResponse;
import com.tripping.backend.insight.service.DateRange;
import com.tripping.backend.insight.service.InsightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

// TripPing-Web(B2B 포털)의 트렌드 화면 전용 API. 로그인 안 한 사람은 SecurityConfig의
// anyRequest().authenticated() 규칙에 걸려 여기 오기 전에 이미 막힌다 (allow-list에 안 넣었음).
@Tag(name = "B2B 인사이트 - 트렌드", description = "기간·지역별 방문 통계와 급상승 루트 랭킹 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/b2b/insight")
public class InsightController {

    private final InsightService insightService;

    // [트렌드 KPI] GET /b2b/insight/trends/summary?period=최근 30일&region=전체 지역&endDate=2026-08-31
    // 프론트 달력에서 시작일까지 직접 고르면 startDate도 같이 오는데, 그러면 period는 무시하고
    // startDate~endDate 구간을 그대로 쓴다 (달력으로 임의 구간 선택하는 용도).
    @Operation(summary = "트렌드 요약(총 방문 핑 + 증감률) 조회")
    @GetMapping("/trends/summary")
    public ResponseEntity<TrendsSummaryResponse> summary(
            @Parameter(description = "\"최근 7일\" / \"최근 30일\" / \"최근 1년\". startDate가 있으면 무시됨")
            @RequestParam(defaultValue = "최근 30일") String period,
            @Parameter(description = "지역명. \"전체 지역\"이면 필터 없음")
            @RequestParam(defaultValue = "전체 지역") String region,
            @Parameter(description = "구간의 기준일(마지막 날). 생략하면 오늘. 미래 날짜는 오늘로 보정됨")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "구간의 시작일. 달력에서 직접 범위를 고를 때만 보냄")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        return ResponseEntity.ok(insightService.summary(region, resolveRange(period, startDate, endDate)));
    }

    // [급상승 루트 랭킹] GET /b2b/insight/trends/routes?period=최근 30일&region=전체 지역&endDate=2026-08-31
    @Operation(summary = "급상승 루트 랭킹 조회 (최대 10개)")
    @GetMapping("/trends/routes")
    public ResponseEntity<List<RouteRankingResponse>> routes(
            @Parameter(description = "\"최근 7일\" / \"최근 30일\" / \"최근 1년\". startDate가 있으면 무시됨")
            @RequestParam(defaultValue = "최근 30일") String period,
            @Parameter(description = "지역명. \"전체 지역\"이면 필터 없음")
            @RequestParam(defaultValue = "전체 지역") String region,
            @Parameter(description = "구간의 기준일(마지막 날). 생략하면 오늘. 미래 날짜는 오늘로 보정됨")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "구간의 시작일. 달력에서 직접 범위를 고를 때만 보냄")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        return ResponseEntity.ok(insightService.risingRoutes(region, resolveRange(period, startDate, endDate)));
    }

    // 프론트 날짜 선택기가 미래 날짜를 막아두긴 하지만, API를 직접 호출하는 경우까지 대비해
    // 서버에서도 한 번 더 막는다 — 미래 날짜가 오면 오늘로 보정. startDate가 endDate보다
    // 늦으면(잘못된 요청) 그냥 하루짜리 구간(endDate~endDate)으로 보정한다.
    private DateRange resolveRange(String period, LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        LocalDate resolvedEnd = (endDate == null || endDate.isAfter(today)) ? today : endDate;
        if (startDate == null) return DateRange.forPeriod(period, resolvedEnd);
        LocalDate resolvedStart = startDate.isAfter(resolvedEnd) ? resolvedEnd : startDate;
        return new DateRange(resolvedStart, resolvedEnd);
    }

    // [일자별 이동량] GET /b2b/insight/trends/daily?period=최근 30일&region=전체 지역
    // summary()의 "총 방문 핑"과 완전히 같은 집계 기준(스팟 체크인 수)을 날짜별로 쪼갠 것.
    @Operation(summary = "일자별 방문 핑(이동량) 추이 조회", description = "선택한 기간 안의 날짜별 방문 핑 수. 방문 기록이 없는 날짜는 0으로 채워서 반환한다.")
    @GetMapping("/trends/daily")
    public ResponseEntity<List<DailyVisitResponse>> daily(
            @Parameter(description = "\"최근 7일\" / \"최근 30일\" / \"최근 1년\"")
            @RequestParam(defaultValue = "최근 30일") String period,
            @Parameter(description = "지역명. \"전체 지역\"이면 필터 없음")
            @RequestParam(defaultValue = "전체 지역") String region) {
        return ResponseEntity.ok(insightService.dailyVisits(period, region));
    }

    // [지역 전체 방문자수 참고선] GET /b2b/insight/trends/regional-visitors?period=최근 30일&region=제주
    // 한국관광공사 "빅데이터 지역별 방문자수(DataLabService)" 기준 국가 통계 방문자수.
    // 우리 자체 방문 핑 데이터(위 daily)와 비교해서 보여주기 위한 것 - 특정 지역을 골랐을 때만
    // 의미가 있어서 "전체 지역"이거나 매핑이 없는 지역이면 빈 리스트를 반환한다.
    @Operation(summary = "지역 전체 방문자수(관광공사 통계) 조회", description = "선택한 지역(시도)의 국가 통계 기준 일자별 방문자수. \"전체 지역\"이거나 관광공사 지역코드가 없는 지역이면 빈 리스트를 반환한다.")
    @GetMapping("/trends/regional-visitors")
    public ResponseEntity<List<RegionalVisitorResponse>> regionalVisitors(
            @Parameter(description = "\"최근 7일\" / \"최근 30일\" / \"최근 1년\"")
            @RequestParam(defaultValue = "최근 30일") String period,
            @Parameter(description = "지역명. \"전체 지역\"이면 빈 리스트 반환")
            @RequestParam(defaultValue = "전체 지역") String region) {
        return ResponseEntity.ok(insightService.regionalVisitors(period, region));
    }
}
