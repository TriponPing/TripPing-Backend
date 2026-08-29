package com.tripping.backend.mypage.controller;

import com.tripping.backend.auth.service.CustomUserDetails;
import com.tripping.backend.mypage.dto.*;
import com.tripping.backend.mypage.service.MyPageMapService;
import com.tripping.backend.mypage.service.MyPageProfileService;
import com.tripping.backend.mypage.service.MyPageRouteService;
import com.tripping.backend.mypage.service.MyPageTripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "마이페이지", description = "프로필, 다녀온 여행, 저장한 루트, 나의 여행 지도 API")
@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageProfileService profileService;
    private final MyPageTripService tripService;
    private final MyPageRouteService routeService;
    private final MyPageMapService mapService;

    @Operation(summary = "프로필 조회", description = "로그인한 유저의 프로필 정보를 조회합니다.")
    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        requireLogin(userDetails);
        return ResponseEntity.ok(profileService.getProfile(userDetails.getUserId()));
    }

    @Operation(summary = "프로필 수정", description = "닉네임/프로필이미지/지역/언어 중 값이 있는 필드만 부분 수정합니다.")
    @PatchMapping
    public ResponseEntity<ProfileResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        requireLogin(userDetails);
        return ResponseEntity.ok(profileService.updateProfile(userDetails.getUserId(), request));
    }

    @Operation(summary = "다녀온 여행 목록 조회(요약)", description = "최근 다녀온 여행 최대 5건을 요약해서 조회합니다.")
    @GetMapping("/trips/recent")
    public ResponseEntity<List<TripSummaryResponse>> getRecentTrips(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        requireLogin(userDetails);
        return ResponseEntity.ok(tripService.getRecentTrips(userDetails.getUserId()));
    }

    @Operation(summary = "다녀온 여행 전체 목록 조회", description = "다녀온 여행 전체 목록을 페이징으로 조회합니다.")
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

    @Operation(summary = "여행 기록 상세 조회", description = "특정 여행의 방문 스팟, 사진, 후기까지 상세 조회합니다.")
    @GetMapping("/trips/{tripId}")
    public ResponseEntity<TripDetailResponse> getTripDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long tripId
    ) {
        requireLogin(userDetails);
        return ResponseEntity.ok(tripService.getTripDetail(userDetails.getUserId(), tripId));
    }

    @Operation(summary = "저장한 루트 목록 조회", description = "북마크한 루트 목록을 페이징으로 조회합니다.")
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

    @Operation(summary = "나의 여행 지도 조회", description = "다녀온 여행 + 저장한 루트를 지도에 찍을 핀 목록(대표 좌표)으로 조회합니다.")
    @GetMapping("/map")
    public ResponseEntity<List<MapPinResponse>> getMyMap(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        requireLogin(userDetails);
        return ResponseEntity.ok(mapService.getMyMap(userDetails.getUserId()));
    }

    @Operation(summary = "나의 여행 지도 상세 조회", description = "type=drawn(내가 다녀온 여행) 또는 saved(저장한 루트)별로 전체 경로를 조회합니다.")
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

    @Operation(summary = "지도 검색", description = "나의 여행 지도(다녀온 여행+저장한 루트)에서 포함된 관광지 이름으로 검색합니다.")
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
