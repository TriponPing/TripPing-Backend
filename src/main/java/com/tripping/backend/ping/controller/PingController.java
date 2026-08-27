package com.tripping.backend.ping.controller;

import com.tripping.backend.auth.service.CustomUserDetails;
import com.tripping.backend.ping.dto.OngoingTripResponse;
import com.tripping.backend.ping.dto.PingRegisterRequest;
import com.tripping.backend.ping.dto.PingResponse;
import com.tripping.backend.ping.service.PingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PingController {

    private final PingService pingService;

    // 방문 장소 Ping 등록
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

    // 진행 중 여행 조회
    @GetMapping("/routes/{routeId}/pings")
    public ResponseEntity<OngoingTripResponse> getOngoingTrip(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long routeId
    ) {
        requireLogin(userDetails);
        return ResponseEntity.ok(pingService.getOngoingTrip(userDetails.getUserId(), routeId));
    }

    // 여행 Ping 기록 조회
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
}
