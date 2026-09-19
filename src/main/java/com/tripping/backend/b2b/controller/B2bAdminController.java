package com.tripping.backend.b2b.controller;

import com.tripping.backend.b2b.dto.OrgReviewRequestDto;
import com.tripping.backend.b2b.service.B2bAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 운영자(Admin) 전용 - 기관 접근 신청 승인/반려.
// ROLE_ADMIN만 접근 가능하도록 SecurityConfig에서 /b2b/admin/** 에
// hasAuthority("ROLE_ADMIN")을 걸어둔다 (이 컨트롤러 자체는 권한 체크 안 함).
@RequiredArgsConstructor
@RestController
@RequestMapping("/b2b/admin")
public class B2bAdminController {

    private final B2bAdminService b2bAdminService;

    // [대기 중인 신청 목록] GET /b2b/admin/organizations
    @GetMapping("/organizations")
    public ResponseEntity<?> pendingRequests() {
        return ResponseEntity.ok(b2bAdminService.pendingRequests());
    }

    // [승인/반려 처리] PATCH /b2b/admin/organizations/{orgId}
    @PatchMapping("/organizations/{orgId}")
    public ResponseEntity<?> review(@PathVariable Long orgId, @RequestBody OrgReviewRequestDto request) {
        return ResponseEntity.ok(b2bAdminService.review(orgId, request));
    }
}
