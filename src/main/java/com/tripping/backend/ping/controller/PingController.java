package com.tripping.backend.ping.controller;

import com.tripping.backend.auth.service.CustomUserDetails;
import com.tripping.backend.ping.dto.OngoingTripResponse;
import com.tripping.backend.ping.dto.PingRegisterRequest;
import com.tripping.backend.ping.dto.PingResponse;
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

    private void requireLogin(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
    }

    // PingController.java 등에 추가

    @GetMapping("/spots/{spotId}/ping-stats")
    public ResponseEntity<SpotPingStatsResponse> getSpotPingStats(@PathVariable Long spotId) {
        SpotPingStatsResponse stats = pingService.getSpotPingStats(spotId);
        return ResponseEntity.ok(stats);
    }
}
