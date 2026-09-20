package com.tripping.backend.b2b.controller;

import com.tripping.backend.b2b.service.B2bAlternativeSpotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

// 상품 일정 구간을 바꿀 때 쓰는 대체 관광지 추천.
//
// 관광지 정보는 기관별로 나뉘는 데이터가 아니라 기관/운영자를 구분하지 않는다.
// 로그인 여부는 SecurityConfig의 anyRequest().authenticated()가 보장한다.
@RequiredArgsConstructor
@RestController
@RequestMapping("/b2b/spots")
public class B2bAlternativeSpotController {

    private final B2bAlternativeSpotService b2bAlternativeSpotService;

    // [대체 관광지 추천] GET /b2b/spots/{spotId}/alternatives?category=음식점&limit=4
    //
    // category를 빼면 카테고리를 가리지 않고 주변에서 찾는다.
    // 정렬은 방문자 수 → 평점 → 거리 순이고, 핑이 쌓이기 전에는
    // 앞의 두 기준이 비어 있어 가까운 곳부터 나온다.
    @GetMapping("/{spotId}/alternatives")
    public ResponseEntity<?> alternatives(@PathVariable Long spotId,
                                          @RequestParam(required = false) String category,
                                          @RequestParam(required = false) Integer limit,
                                          @RequestParam(required = false) Double radiusKm) {
        return ResponseEntity.ok(
                b2bAlternativeSpotService.findAlternatives(spotId, category, limit, radiusKm));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
