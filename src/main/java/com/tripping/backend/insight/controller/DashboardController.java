package com.tripping.backend.insight.controller;

import com.tripping.backend.insight.dto.DashboardSummaryResponse;
import com.tripping.backend.insight.dto.RegionRankResponse;
import com.tripping.backend.insight.dto.RouteNetworkResponse;
import com.tripping.backend.insight.service.DashboardService;
import com.tripping.backend.insight.service.DateRange;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

// TripPing-Web(B2B 포털)의 대시보드 화면 전용 API.
// 기간·지역 파라미터 규약은 InsightController(트렌드)와 완전히 동일하게 맞췄다 —
// 두 화면이 같은 기간을 보면 "총 방문 핑" 숫자가 반드시 일치해야 하기 때문.
@Tag(name = "B2B 인사이트 - 대시보드", description = "대시보드 KPI·지역 랭킹·이동 네트워크 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/b2b/insight/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    // [대시보드 KPI] GET /b2b/insight/dashboard/summary?period=최근 30일&region=전체 지역
    @Operation(summary = "대시보드 KPI 조회",
            description = "총 방문 핑·활성 여행객·기록된 루트·여행당 평균 방문 관광지 수·평균 만족도를 "
                    + "직전 동일 길이 구간 대비 증감률과 함께 반환한다.")
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> summary(
            @Parameter(description = "\"최근 7일\" / \"최근 30일\" / \"최근 1년\". startDate가 있으면 무시됨")
            @RequestParam(defaultValue = "최근 30일") String period,
            @Parameter(description = "지역명. \"전체 지역\"이면 필터 없음")
            @RequestParam(defaultValue = "전체 지역") String region,
            @Parameter(description = "구간의 기준일(마지막 날). 생략하면 오늘. 미래 날짜는 오늘로 보정됨")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "구간의 시작일. 달력에서 직접 범위를 고를 때만 보냄")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        return ResponseEntity.ok(dashboardService.summary(region, resolveRange(period, startDate, endDate)));
    }

    // [지역별 인기] GET /b2b/insight/dashboard/regions?period=최근 30일
    // 지역끼리 비교하는 게 목적이라 region 필터는 받지 않는다.
    @Operation(summary = "지역별 인기 랭킹 조회",
            description = "시도별로 Trip Ping 자체 방문 핑 수와 한국관광공사 DataLab 기준 방문자수를 함께 반환한다. "
                    + "정렬 기준은 관광공사 방문자수.")
    @GetMapping("/regions")
    public ResponseEntity<List<RegionRankResponse>> regions(
            @Parameter(description = "\"최근 7일\" / \"최근 30일\" / \"최근 1년\". startDate가 있으면 무시됨")
            @RequestParam(defaultValue = "최근 30일") String period,
            @Parameter(description = "구간의 기준일(마지막 날). 생략하면 오늘. 미래 날짜는 오늘로 보정됨")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "구간의 시작일. 달력에서 직접 범위를 고를 때만 보냄")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        return ResponseEntity.ok(dashboardService.regionRanking(resolveRange(period, startDate, endDate)));
    }

    // [이동 네트워크] GET /b2b/insight/dashboard/network?period=최근 30일&region=전체 지역
    @Operation(summary = "여행 루트 네트워크 조회",
            description = "실제 방문 기록에서 뽑은 관광지 노드(좌표·방문 핑 수)와 "
                    + "같은 여행에서 이어서 방문한 관광지 쌍(엣지·관측 횟수)을 반환한다. "
                    + "좌표가 없는 관광지와 그에 붙은 연결은 제외된다.")
    @GetMapping("/network")
    public ResponseEntity<RouteNetworkResponse> network(
            @Parameter(description = "\"최근 7일\" / \"최근 30일\" / \"최근 1년\". startDate가 있으면 무시됨")
            @RequestParam(defaultValue = "최근 30일") String period,
            @Parameter(description = "지역명. \"전체 지역\"이면 필터 없음")
            @RequestParam(defaultValue = "전체 지역") String region,
            @Parameter(description = "구간의 기준일(마지막 날). 생략하면 오늘. 미래 날짜는 오늘로 보정됨")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "구간의 시작일. 달력에서 직접 범위를 고를 때만 보냄")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        return ResponseEntity.ok(dashboardService.routeNetwork(region, resolveRange(period, startDate, endDate)));
    }

    // InsightController.resolveRange()와 같은 규칙 — 미래 날짜는 오늘로, 시작일이 종료일보다
    // 늦으면 하루짜리 구간으로 보정한다. 두 컨트롤러의 구간 계산이 어긋나면 같은 기간인데도
    // 화면마다 숫자가 달라지므로 규칙을 일부러 똑같이 유지한다.
    private DateRange resolveRange(String period, LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        LocalDate resolvedEnd = (endDate == null || endDate.isAfter(today)) ? today : endDate;
        if (startDate == null) return DateRange.forPeriod(period, resolvedEnd);
        LocalDate resolvedStart = startDate.isAfter(resolvedEnd) ? resolvedEnd : startDate;
        return new DateRange(resolvedStart, resolvedEnd);
    }
}
