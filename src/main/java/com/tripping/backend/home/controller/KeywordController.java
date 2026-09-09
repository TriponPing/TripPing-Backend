package com.tripping.backend.home.controller;

import com.tripping.backend.home.dto.response.PopularKeywordResponse;
import com.tripping.backend.home.dto.response.PopularTripResponse;
import com.tripping.backend.home.service.PopularKeywordService;
import com.tripping.backend.home.service.PopularTripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "홈 - 키워드")
@RestController
@RequestMapping("/keyword")
@RequiredArgsConstructor
public class KeywordController {

    private final PopularKeywordService popularKeywordService;
    private final PopularTripService popularTripService;

    @Operation(summary = "인기 키워드 조회", description = "최근 7일간 Ping 후기에 달린 해시태그의 등장 빈도 기준으로 인기 키워드를 조회합니다.")
    @GetMapping("/popular")
    public ResponseEntity<List<PopularKeywordResponse>> getPopularKeywords(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(popularKeywordService.getPopularKeywords(limit));
    }

    @Operation(summary = "키워드로 루트 조회", description = "이 해시태그가 달린 후기가 있는 공개 루트 목록을 조회합니다.")
    @GetMapping("/{keyword}/routes")
    public ResponseEntity<List<PopularTripResponse>> getRoutesByKeyword(
            @PathVariable String keyword,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(popularTripService.getRoutesByKeyword(keyword, limit));
    }
}
