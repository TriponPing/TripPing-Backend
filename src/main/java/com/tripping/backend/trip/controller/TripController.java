package com.tripping.backend.trip.controller;

import com.tripping.backend.auth.service.CustomUserDetails;
import com.tripping.backend.entity.ActualRoute;
import com.tripping.backend.trip.dto.TripCreateRequest;
import com.tripping.backend.trip.dto.TripResponse;
import com.tripping.backend.trip.dto.TripRouteMapSearchResponse;
import com.tripping.backend.auth.service.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.tripping.backend.trip.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
@Tag(name = "루트", description = "여행(Trip) 전환 API")
public class TripController {

    private final TripService tripService;

    @PostMapping("/{routeId}/trips")
    @Operation(summary = "실제 여행으로 저장")
    public ResponseEntity<TripResponse> createTrip(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long routeId,
            @RequestBody TripCreateRequest request
    ) {
        return ResponseEntity.ok(tripService.createTrip(userDetails.getUserId(), routeId, request));
    }

    @GetMapping("/map/search")
    @Operation(summary = "지도용 루트 검색", description = "지역/카테고리 조건에 맞는 공개 루트들을 지도 표시용 데이터로 반환")
    public ResponseEntity<java.util.List<TripRouteMapSearchResponse>> searchRoutesForMap(
            @RequestParam(required = false) String regionId,
            @RequestParam(required = false) String category
    ) {
        return ResponseEntity.ok(tripService.searchRoutesForMap(regionId, category));
    }

    // ⭐️ [추가] Ping 탭 등에서 id 없이도 현재 진행 중인 여행을 스스로 조회하는 API
    @GetMapping("/current")
    @Operation(summary = "진행 중인 여행 조회", description = "유저의 IN_PROGRESS 상태인 최신 여행 정보를 조회합니다.")
    public ResponseEntity<ActualRoute> getCurrentRoute(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails != null ? userDetails.getUserId() : 1L; // 임시 방어 코드

        ActualRoute response = tripService.getCurrentInProgressRoute(userId); // 서비스 메서드명 일치화

        if (response == null) {
            return ResponseEntity.noContent().build(); // 진행 중인 여행이 없으면 204 No Content
        }
        return ResponseEntity.ok(response); // 있으면 200 OK + 데이터
    }

    @Operation(summary = "내가 저장한 루트 지도 조회")
    @GetMapping("/saved/map")
    public ResponseEntity<List<TripRouteMapSearchResponse>> getMySavedRoutesForMap(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(tripService.getSavedRoutesForMap(userDetails.getUserId()));
    }
}