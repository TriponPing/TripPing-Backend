package com.tripping.backend.b2b.controller;

import com.tripping.backend.b2b.dto.OrgJoinRequestDto;
import com.tripping.backend.b2b.dto.OrgLoginRequestDto;
import com.tripping.backend.b2b.service.B2bAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

// TripPing-Web(B2B 인사이트 포털)이 쓰는 인증 API.
// 앱이 쓰는 /auth/* 와는 완전히 별도 경로 — 로그인 대상이 AppUser가 아니라
// Organization(기관 담당자) 또는 Admin(운영자)이기 때문.
@RequiredArgsConstructor
@RestController
@RequestMapping("/b2b/auth")
public class B2bAuthController {

    private final B2bAuthService b2bAuthService;

    // [기관 접근 신청] POST /b2b/auth/join
    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestBody OrgJoinRequestDto request) {
        b2bAuthService.join(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("접근 신청 접수 완료");
    }

    // [기관/운영자 통합 로그인] POST /b2b/auth/login
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody OrgLoginRequestDto request, HttpServletRequest httpRequest) {
        b2bAuthService.login(request, httpRequest);
        return ResponseEntity.ok("로그인 성공");
    }

    // [로그아웃] POST /b2b/auth/logout
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        request.getSession().invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("로그아웃 완료");
    }

    // [내 상태 확인] GET /b2b/auth/me
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        return ResponseEntity.ok(b2bAuthService.me(authentication));
    }
}
