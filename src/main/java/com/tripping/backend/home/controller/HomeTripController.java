package com.tripping.backend.home.controller;

import com.tripping.backend.home.dto.response.NearbyTripResponse;
import com.tripping.backend.home.dto.response.PopularTripResponse;
import com.tripping.backend.home.service.NearbyTripService;
import com.tripping.backend.home.service.PopularTripService;
import com.tripping.backend.mypage.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 클래스명이 trip 패키지의 TripController(실제 여행 전환 API, "/routes/{routeId}/trips")와
 * 겹치지 않도록 Home 접두사를 붙였습니다. URL 경로("/trips/popular", "/trips/nearby")는
 * 기획서 스펙 그대로입니다.
 */
@Tag(name = "홈 - 여행")
@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
public class HomeTripController {

    private final PopularTripService popularTripService;
    private final NearbyTripService nearbyTripService;

    @Operation(summary = "이번주 인기 여행 조회", description = "저장(찜) 수 기준으로 이번 주 인기 있는 루트를 조회합니다.")
    @GetMapping("/popular")
    public ResponseEntity<List<PopularTripResponse>> getPopularTrips(
            @RequestParam(defaultValue = "week") String period,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(popularTripService.getPopularTrips(period, limit));
    }

    @Operation(summary = "내 주변 여행 조회", description = "실제 방문 GPS 기준으로 현재 위치에서 가까운 공개 루트를 거리순으로 조회합니다.")
    @GetMapping("/nearby")
    public ResponseEntity<PageResponse<NearbyTripResponse>> getNearbyTrips(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(required = false) Double radiusKm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(nearbyTripService.getNearbyTrips(lat, lng, radiusKm, page, size));
    }
}
