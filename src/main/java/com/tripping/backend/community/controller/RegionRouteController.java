package com.tripping.backend.community.controller;

import com.tripping.backend.community.dto.response.RouteSummaryResponse;
import com.tripping.backend.community.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "커뮤니티 - 루트")
@RestController
@RequestMapping("/regions")
@RequiredArgsConstructor
public class RegionRouteController {

    private final RouteService routeService;

    @Operation(summary = "지역별 루트 조회", description = "특정 지역을 지나간 공개 루트 목록을 조회합니다.")
    @GetMapping("/{regionId}/routes")
    public ResponseEntity<Page<RouteSummaryResponse>> getRoutesByRegion(
            @PathVariable String regionId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(routeService.getRoutesByRegion(regionId, pageable));
    }
}
