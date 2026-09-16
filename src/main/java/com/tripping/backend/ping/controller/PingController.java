package com.tripping.backend.ping.controller;

import com.tripping.backend.auth.service.CustomUserDetails;
import com.tripping.backend.ping.dto.AddTripSpotRequest;
import com.tripping.backend.ping.dto.AddTripSpotResponse;
import com.tripping.backend.ping.dto.ConfirmNextPingResponse;
import com.tripping.backend.ping.dto.OngoingTripResponse;
import com.tripping.backend.ping.dto.PingRegisterRequest;
import com.tripping.backend.ping.dto.PingResponse;
import com.tripping.backend.ping.dto.ReorderTripSpotsRequest;
import com.tripping.backend.ping.dto.SpotPingStatsResponse;
import com.tripping.backend.ping.service.PingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Tag(name = "Ping", description = "여행 중 방문 장소 Ping 등록/조회 API")
@RestController
@RequiredArgsConstructor
public class PingController {

    private final PingService pingService;

    @Operation(summary = "방문 장소 Ping 등록", description = "진행 중인 여행에 실시간으로 방문 장소를 핑 찍어 등록합니다.")
    @PostMapping("/routes/{routeId}/pings")
    public ResponseEntity<PingResponse> registerPing(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long routeId,
            @Valid @RequestBody PingRegisterRequest request
    ) {
        requireLogin(userDetails);
        PingResponse response = pingService.registerPing(userDetails.getUserId(), routeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "진행 중 여행 조회", description = "여행 정보와 지금까지 등록된 핑 목록을 함께 조회합니다.")
    @GetMapping("/routes/{routeId}/pings")
    public ResponseEntity<OngoingTripResponse> getOngoingTrip(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long routeId
    ) {
        requireLogin(userDetails);
        return ResponseEntity.ok(pingService.getOngoingTrip(userDetails.getUserId(), routeId));
    }

    @Operation(summary = "여행 Ping 기록 조회", description = "진행중/완료 상관없이 여행에 등록된 핑 전체 기록을 조회합니다.")
    @GetMapping("/trips/{routeId}/pings")
    public ResponseEntity<List<PingResponse>> getTripPings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long routeId
    ) {
        requireLogin(userDetails);
        return ResponseEntity.ok(pingService.getTripPings(userDetails.getUserId(), routeId));
    }

    // 👈 새로 추가: 완료된 여행에 놓친 방문 스팟 추가
    @Operation(summary = "여행에 방문 스팟 추가", description = "완료된 여행에 나중에 빠뜨린 방문 장소를 추가합니다. (ACTUAL_ROUTE_SPOT에 바로 저장)")
    @PostMapping("/trips/{routeId}/spots")
    public ResponseEntity<AddTripSpotResponse> addTripSpot(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long routeId,
            @Valid @RequestBody AddTripSpotRequest request
    ) {
        requireLogin(userDetails);
        AddTripSpotResponse response = pingService.addSpotToTrip(userDetails.getUserId(), routeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 👈 새로 추가: 여행 기록(방문 스팟) 삭제
    @Operation(summary = "여행 기록 삭제", description = "실수로 잘못 찍은 방문 스팟 기록을 삭제합니다. (ACTUAL_ROUTE_SPOT)")
    @DeleteMapping("/trips/{routeId}/spots/{actualRouteSpotId}")
    public ResponseEntity<Void> deleteTripSpot(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long routeId,
            @PathVariable Long actualRouteSpotId
    ) {
        requireLogin(userDetails);
        pingService.deleteSpotFromTrip(userDetails.getUserId(), routeId, actualRouteSpotId);
        return ResponseEntity.noContent().build();
    }

    // 👈 새로 추가: 계획된 순서대로 다음 핑 찍기 (큐)
    @Operation(summary = "다음 핑 찍기", description = "진행 중인 여행에서 계획된 방문 순서상 다음 장소 하나를 핑으로 확정합니다.")
    @PostMapping("/routes/{routeId}/pings/next")
    public ResponseEntity<ConfirmNextPingResponse> confirmNextPing(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long routeId
    ) {
        requireLogin(userDetails);
        ConfirmNextPingResponse response = pingService.confirmNextPing(userDetails.getUserId(), routeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 👈 새로 추가: 여행 기록(방문 스팟) 순서 변경 - 꾹 눌러 드래그로 재정렬한 새 순서 저장
    @Operation(summary = "여행 기록 순서 변경", description = "핑 기록(ACTUAL_ROUTE_SPOT)의 방문 순서를 드래그로 재정렬한 새 순서로 저장합니다.")
    @PatchMapping("/trips/{routeId}/spots/order")
    public ResponseEntity<Void> reorderTripSpots(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long routeId,
            @Valid @RequestBody ReorderTripSpotsRequest request
    ) {
        requireLogin(userDetails);
        pingService.reorderTripSpots(userDetails.getUserId(), routeId, request);
        return ResponseEntity.noContent().build();
    }

    private void requireLogin(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
    }

    @Operation(summary = "장소별 핑 통계 조회", description = "특정 장소의 실시간 인기 시간대 및 총 핑 개수를 조회합니다.")
    @GetMapping("/spots/{spotId}/ping-stats")
    public ResponseEntity<SpotPingStatsResponse> getSpotPingStats(@PathVariable Long spotId) {
        SpotPingStatsResponse stats = pingService.getSpotPingStats(spotId);
        return ResponseEntity.ok(stats);
    }
}