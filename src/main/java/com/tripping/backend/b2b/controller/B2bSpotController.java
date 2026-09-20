package com.tripping.backend.b2b.controller;

import com.tripping.backend.b2b.service.B2bSpotSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

// 상품 일정에 넣을 관광지 검색.
//
// SecurityConfig의 anyRequest().authenticated()에 걸려 로그인은 보장된다.
// 관광지 정보 자체는 기관별로 나뉘는 데이터가 아니라 기관/운영자를 구분하지 않는다.
@RequiredArgsConstructor
@RestController
@RequestMapping("/b2b/spots")
public class B2bSpotController {

    private final B2bSpotSearchService b2bSpotSearchService;

    // [관광지 검색] GET /b2b/spots/search?query=울산
    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String query) {
        return ResponseEntity.ok(b2bSpotSearchService.search(query));
    }

    // [관광공사 관광지 등록] POST /b2b/spots/register?contentId=126508
    // 검색 결과에서 registered=false인 항목을 일정에 담기 직전에 부른다.
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam String contentId) {
        return ResponseEntity.ok(b2bSpotSearchService.register(contentId));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
