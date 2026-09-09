package com.tripping.backend.home.controller;

import com.tripping.backend.auth.service.CustomUserDetails;
import com.tripping.backend.home.dto.response.PopularPlaceResponse;
import com.tripping.backend.home.dto.response.TrendingPlaceResponse;
import com.tripping.backend.home.service.PopularPlaceService;
import com.tripping.backend.home.service.TrendingPlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "홈 - 장소")
@RestController
@RequestMapping("/places")
@RequiredArgsConstructor
public class PlaceController {

    private final TrendingPlaceService trendingPlaceService;
    private final PopularPlaceService popularPlaceService;

    @Operation(summary = "떠오르는 인기 장소 조회", description = "최근 방문(핑) 횟수가 많은 관광지를 조회합니다.")
    @GetMapping("/trending")
    public ResponseEntity<List<TrendingPlaceResponse>> getTrendingPlaces(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(trendingPlaceService.getTrendingPlaces(limit));
    }

    @Operation(summary = "이번주 인기 장소 TOP N 조회", description = "이번 주 저장(찜) 수가 많은 순으로 조회합니다. 기본 30개.")
    @GetMapping("/popular")
    public ResponseEntity<List<PopularPlaceResponse>> getPopularPlaces(
            @RequestParam(defaultValue = "30") int limit
    ) {
        return ResponseEntity.ok(popularPlaceService.getPopularPlaces(limit));
    }

    @Operation(summary = "내가 저장한 장소 id 목록 조회", description = "북마크(저장)한 장소의 spotId 목록을 조회합니다. 화면 진입 시 북마크 초기 상태 복원용.")
    @GetMapping("/saved/me/ids")
    public ResponseEntity<List<Long>> getMySavedPlaceIds(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return ResponseEntity.ok(popularPlaceService.getMySavedSpotIds(userDetails.getUserId()));
    }
}
