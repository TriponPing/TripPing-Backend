package com.tripping.backend.route.controller;

import com.tripping.backend.route.dto.RegionSearchResponse;
import com.tripping.backend.route.service.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/regions")
@RequiredArgsConstructor
@Tag(name = "루트", description = "지역 검색 API")
public class RegionController {

    private final RegionService regionService;

    @GetMapping("/search")
    @Operation(summary = "여행 지역 검색")
    public ResponseEntity<List<RegionSearchResponse>> searchRegions(@RequestParam String keyword) {
        return ResponseEntity.ok(regionService.searchRegions(keyword));
    }
}