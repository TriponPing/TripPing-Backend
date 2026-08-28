package com.tripping.backend.place.controller;

import com.tripping.backend.place.dto.TouristSpotResponse;
import com.tripping.backend.place.service.TouristSpotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
    @Operation(summary = "탐색 지도 필터 기능", description = "스펙상 PATCH였으나 조회 동작이라 GET으로 변경. 현재 지역 필터만 지원, 혼잡도/시간대/Ping수는 추후 반영")
    public ResponseEntity<List<TouristSpotResponse>> getByCategoryAndFilters(
            @RequestParam String category,
            @RequestParam(required = false) String regionId
    ) {
        return ResponseEntity.ok(touristSpotService.getByCategoryAndFilters(category, regionId));
    }

    @GetMapping("/places/{placeId}")
    @Operation(summary = "장소 상세 조회")
    public ResponseEntity<TouristSpotResponse> getDetail(
            @PathVariable Long placeId
    ) {
        return ResponseEntity.ok(touristSpotService.getDetail(placeId));
    }
}