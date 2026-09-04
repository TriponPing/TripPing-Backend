package com.tripping.backend.home.controller;

import com.tripping.backend.home.dto.response.PopularKeywordResponse;
import com.tripping.backend.home.service.PopularKeywordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "홈 - 키워드")
@RestController
@RequestMapping("/keyword")
@RequiredArgsConstructor
public class KeywordController {

    private final PopularKeywordService popularKeywordService;

    @Operation(summary = "인기 키워드 조회", description = "루트 조건(테마) 값의 등장 빈도 기준으로 인기 키워드를 조회합니다.")
    @GetMapping("/popular")
    public ResponseEntity<List<PopularKeywordResponse>> getPopularKeywords(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(popularKeywordService.getPopularKeywords(limit));
    }
}
