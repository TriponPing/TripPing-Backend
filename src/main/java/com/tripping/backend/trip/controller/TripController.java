package com.tripping.backend.trip.controller;

import com.tripping.backend.auth.service.CustomUserDetails;
import com.tripping.backend.trip.dto.TripCreateRequest;
import com.tripping.backend.trip.dto.TripResponse;
import com.tripping.backend.trip.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
}