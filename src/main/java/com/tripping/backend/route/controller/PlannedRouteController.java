package com.tripping.backend.route.controller;

import com.tripping.backend.auth.service.CustomUserDetails;
import com.tripping.backend.route.dto.*;
import com.tripping.backend.route.service.PlannedRouteService;
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
@Tag(name = "루트", description = "루트 생성/조회 및 장소 CRUD API")
public class PlannedRouteController {

    private final PlannedRouteService plannedRouteService;

    @PostMapping
    @Operation(summary = "실제 루트로 저장")
    public ResponseEntity<RouteResponse> createRoute(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody RouteCreateRequest request
    ) {
        return ResponseEntity.ok(plannedRouteService.createRoute(userDetails.getUserId(), request));
    }

    @GetMapping("/{routeId}")
    @Operation(summary = "루트 상세 조회")
    public ResponseEntity<RouteResponse> getRouteDetail(@PathVariable Long routeId) {
        return ResponseEntity.ok(plannedRouteService.getRouteDetail(routeId));
    }

//    @GetMapping("/{routeId}/map")
//    @Operation(summary = "지도 불러오기 (저장된 장소 포함)")
//    public ResponseEntity<RouteMapResponse> getRouteMap(@PathVariable Long routeId) {
//        return ResponseEntity.ok(plannedRouteService.getRouteMap(routeId));
//    }

    @PostMapping("/{routeId}/places")
    @Operation(summary = "장소 추가")
    public ResponseEntity<RoutePlaceResponse> addPlace(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long routeId,
            @RequestBody RoutePlaceRequest request
    ) {
        return ResponseEntity.ok(plannedRouteService.addPlace(userDetails.getUserId(), routeId, request));
    }

    @PatchMapping("/{routeId}/places/{routePlaceId}")
    @Operation(summary = "장소 순서, 내용 수정")
    public ResponseEntity<RoutePlaceResponse> updatePlace(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long routeId,
            @PathVariable Long routePlaceId,
            @RequestBody RoutePlaceUpdateRequest request
    ) {
        return ResponseEntity.ok(
                plannedRouteService.updatePlace(userDetails.getUserId(), routeId, routePlaceId, request)
        );
    }

    @DeleteMapping("/{routeId}/places/{routePlaceId}")
    @Operation(summary = "장소 삭제")
    public ResponseEntity<Void> deletePlace(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long routeId,
            @PathVariable Long routePlaceId
    ) {
        plannedRouteService.deletePlace(userDetails.getUserId(), routeId, routePlaceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "루트 이름으로 검색")
    public ResponseEntity<List<RouteSearchResponse>> searchRoutes(
            @RequestParam String keyword
    ) {
        return ResponseEntity.ok(plannedRouteService.searchRoutesByTitle(keyword));
    }
}