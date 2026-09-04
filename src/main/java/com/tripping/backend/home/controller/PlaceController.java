package com.tripping.backend.home.controller;

import com.tripping.backend.home.dto.response.TrendingPlaceResponse;
import com.tripping.backend.home.service.TrendingPlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "홈 - 장소")
@RestController
@RequestMapping("/places")
@RequiredArgsConstructor
public class PlaceController {

    private final TrendingPlaceService trendingPlaceService;

    @Operation(summary = "떠오르는 인기 장소 조회", description = "최근 방문(핑) 횟수가 많은 관광지를 조회합니다.")
    @GetMapping("/trending")
    public ResponseEntity<List<TrendingPlaceResponse>> getTrendingPlaces(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(trendingPlaceService.getTrendingPlaces(limit));
    }
}
