package com.tripping.backend.route.controller;

import com.tripping.backend.route.dto.RouteCandidateResponse;
import com.tripping.backend.route.dto.RouteRecommendRequest;
import com.tripping.backend.route.service.RouteRecommendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
@Tag(name = "루트", description = "루트 추천 API")
public class RouteRecommendController {

    private final RouteRecommendService routeRecommendService;

    @PostMapping("/recommend")
    @Operation(summary = "맞춤 여행 루트 추천 (5개 후보 반환)")
    public ResponseEntity<List<RouteCandidateResponse>> recommend(
            @RequestBody RouteRecommendRequest request
    ) {
        return ResponseEntity.ok(routeRecommendService.recommend(request));
    }
}