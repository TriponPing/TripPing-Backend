package com.tripping.backend.route.controller;

import com.tripping.backend.route.dto.RegionSearchResponse;
import com.tripping.backend.route.service.RegionService;
<<<<<<< Updated upstream
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
=======
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/regions")
>>>>>>> Stashed changes
public class RegionController {

    private final RegionService regionService;

    @GetMapping("/search")
<<<<<<< Updated upstream
    @Operation(summary = "여행 지역 검색")
    public ResponseEntity<List<RegionSearchResponse>> searchRegions(@RequestParam String keyword) {
        return ResponseEntity.ok(regionService.searchRegions(keyword));
=======
    public List<RegionSearchResponse> searchRegions(
            @RequestParam String keyword
    ) {
        return regionService.searchRegions(keyword);
>>>>>>> Stashed changes
    }
}