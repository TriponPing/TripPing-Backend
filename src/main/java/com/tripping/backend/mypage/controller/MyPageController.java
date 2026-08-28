package com.tripping.backend.mypage.controller;

import com.tripping.backend.auth.service.CustomUserDetails;
import com.tripping.backend.mypage.dto.*;
import com.tripping.backend.mypage.service.MyPageMapService;
import com.tripping.backend.mypage.service.MyPageProfileService;
import com.tripping.backend.mypage.service.MyPageRouteService;
import com.tripping.backend.mypage.service.MyPageTripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageProfileService profileService;
    private final MyPageTripService tripService;
    private final MyPageRouteService routeService;
    private final MyPageMapService mapService;

    // 프로필 조회
    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        requireLogin(userDetails);
        return ResponseEntity.ok(profileService.getProfile(userDetails.getUserId()));
    }

    // 프로필 수정
    @PatchMapping
    public ResponseEntity<ProfileResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        requireLogin(userDetails);
        return ResponseEntity.ok(profileService.updateProfile(userDetails.getUserId(), request));
    }

    // 다녀온 여행 목록 조회(요약)
    @GetMapping("/trips/recent")
    public ResponseEntity<List<TripSummaryResponse>> getRecentTrips(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        requireLogin(userDetails);
        return ResponseEntity.ok(tripService.getRecentTrips(userDetails.getUserId()));
    }

    // 다녀온 여행 전체 목록 조회
    @GetMapping("/trips")
    public ResponseEntity<PageResponse<TripSummaryResponse>> getAllTrips(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        requireLogin(userDetails);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(tripService.getAllTrips(userDetails.getUserId(), pageable));
    }

    // 여행 기록 상세 조회
    @GetMapping("/trips/{tripId}")
    public ResponseEntity<TripDetailResponse> getTripDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long tripId
    ) {
        requireLogin(userDetails);
        return ResponseEntity.ok(tripService.getTripDetail(userDetails.getUserId(), tripId));
    }

    // 저장한 루트 목록 조회
    @GetMapping("/routes/saved")
    public ResponseEntity<PageResponse<SavedRouteResponse>> getSavedRoutes(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        requireLogin(userDetails);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(routeService.getSavedRoutes(userDetails.getUserId(), pageable));
    }

    // 나의 여행 지도 조회
    @GetMapping("/map")
    public ResponseEntity<List<MapPinResponse>> getMyMap(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        requireLogin(userDetails);
        return ResponseEntity.ok(mapService.getMyMap(userDetails.getUserId()));
    }

    // 나의 여행 지도 상세 조회
    @GetMapping("/map/detail")
    public ResponseEntity<List<MapDetailResponse>> getMyMapDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String type
    ) {
        requireLogin(userDetails);
        if (!"drawn".equalsIgnoreCase(type) && !"saved".equalsIgnoreCase(type)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "type은 drawn 또는 saved만 가능합니다.");
        }
        return ResponseEntity.ok(mapService.getMyMapDetail(userDetails.getUserId(), type));
    }

    // 지도 검색 (스펙: /trips/search?name= 과 파라미터명 통일)
    @GetMapping("/map/search")
    public ResponseEntity<List<MapSearchResponse>> searchMap(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("name") String keyword
    ) {
        requireLogin(userDetails);
        return ResponseEntity.ok(mapService.search(userDetails.getUserId(), keyword));
    }

    private void requireLogin(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
    }
}
