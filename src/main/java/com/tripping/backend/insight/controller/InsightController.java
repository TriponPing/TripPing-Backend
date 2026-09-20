package com.tripping.backend.insight.controller;

import com.tripping.backend.insight.dto.RouteRankingResponse;
import com.tripping.backend.insight.dto.TrendsSummaryResponse;
import com.tripping.backend.insight.service.InsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// TripPing-Web(B2B 포털)의 트렌드 화면 전용 API. 로그인 안 한 사람은 SecurityConfig의
// anyRequest().authenticated() 규칙에 걸려 여기 오기 전에 이미 막힌다 (allow-list에 안 넣었음).
@RequiredArgsConstructor
@RestController
@RequestMapping("/b2b/insight")
public class InsightController {

    private final InsightService insightService;

    // [트렌드 KPI] GET /b2b/insight/trends/summary?period=최근 30일&region=전체 지역
    @GetMapping("/trends/summary")
    public ResponseEntity<TrendsSummaryResponse> summary(
            @RequestParam(defaultValue = "최근 30일") String period,
            @RequestParam(defaultValue = "전체 지역") String region) {
        return ResponseEntity.ok(insightService.summary(period, region));
    }

    // [급상승 루트 랭킹] GET /b2b/insight/trends/routes?period=최근 30일&region=전체 지역
    @GetMapping("/trends/routes")
    public ResponseEntity<List<RouteRankingResponse>> routes(
            @RequestParam(defaultValue = "최근 30일") String period,
            @RequestParam(defaultValue = "전체 지역") String region) {
        return ResponseEntity.ok(insightService.risingRoutes(period, region));
    }
}
