package com.tripping.backend.place.controller;

import com.tripping.backend.place.dto.CreatePlaceRequest;
import com.tripping.backend.place.dto.TouristSpotResponse;
import com.tripping.backend.place.service.TouristSpotService;
import com.tripping.backend.place.dto.SpotDetailResponse;
import com.tripping.backend.auth.service.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "탐색", description = "장소 조회/검색 API")
public class TouristSpotController {

    private final TouristSpotService touristSpotService;

    @GetMapping("/map/places/search")
    @Operation(summary = "지도 기반 장소 검색")
    public ResponseEntity<List<TouristSpotResponse>> searchByLocation(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam double radius
    ) {
        return ResponseEntity.ok(touristSpotService.searchByLocation(lat, lng, radius));
    }

    @GetMapping("/map/places")
    @Operation(summary = "카테고리별 장소 조회 (관광지/맛집/카페)")
    public ResponseEntity<List<TouristSpotResponse>> getByCategory(
            @RequestParam String category
    ) {
        return ResponseEntity.ok(touristSpotService.getByCategory(category));
    }

    @GetMapping("/places")
    @Operation(summary = "탐색 지도 필터 기능", description = "지역/시간대/핑개수 필터 지원")
    public ResponseEntity<List<TouristSpotResponse>> getByCategoryAndFilters(
            @RequestParam String category,
            @RequestParam(required = false) String regionId,
            @RequestParam(required = false) String timeSlot,
            @RequestParam(required = false) Integer minPingCount
    ) {
        return ResponseEntity.ok(
                touristSpotService.getByCategoryAndFilters(category, regionId, timeSlot, minPingCount)
        );
    }

    @GetMapping("/places/{placeId}")
    @Operation(summary = "장소 상세 조회")
    public ResponseEntity<TouristSpotResponse> getDetail(
            @PathVariable Long placeId
    ) {
        return ResponseEntity.ok(touristSpotService.getDetail(placeId));
    }

    // 새로 추가: 새 장소 등록 - POST /places
    @PostMapping("/places")
    @Operation(summary = "새 장소 등록", description = "네이버맵 등에서 발견한, 아직 우리 DB에 없는 장소를 새로 등록합니다.")
    public ResponseEntity<TouristSpotResponse> createPlace(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreatePlaceRequest request
    ) {
        TouristSpotResponse response = touristSpotService.createPlace(userDetails.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/places/search")
    @Operation(summary = "이름으로 장소 검색", description = "루트에 추가할 장소를 이름으로 검색")
    public ResponseEntity<List<TouristSpotResponse>> searchByKeyword(
            @RequestParam String query,
            @RequestParam(required = false) String regionId
    ) {
        return ResponseEntity.ok(touristSpotService.searchByKeyword(query, regionId));
    }

    @GetMapping("/places/{placeId}/detail")
    @Operation(summary = "장소 상세 조회", description = "통계(핑수/인기시간대), 장소 설명, 등록된 루트, 후기 포함")
    public ResponseEntity<SpotDetailResponse> getSpotDetail(
            @PathVariable Long placeId
    ) {
        return ResponseEntity.ok(touristSpotService.getSpotDetail(placeId));
    }

}