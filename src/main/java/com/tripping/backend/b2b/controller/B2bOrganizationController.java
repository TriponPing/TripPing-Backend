package com.tripping.backend.b2b.controller;

import com.tripping.backend.b2b.dto.ApiKeyCreateRequest;
import com.tripping.backend.b2b.dto.NotificationSettingUpdateRequest;
import com.tripping.backend.b2b.dto.OrgProfileUpdateRequest;
import com.tripping.backend.b2b.service.B2bOrganizationService;
import com.tripping.backend.entity.Organization;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

// 기관 전용 - 조직 설정 (기관 정보 / 알림 설정 / API 키).
//
// 운영자(Admin)도 로그인 상태이므로 기관 계정인지 여기서 확인한다.
@RequiredArgsConstructor
@RestController
@RequestMapping("/b2b/organization")
public class B2bOrganizationController {

    private final B2bOrganizationService b2bOrganizationService;

    // [기관 정보 조회] GET /b2b/organization
    @GetMapping
    public ResponseEntity<?> profile(Authentication authentication) {
        Organization org = organizationOf(authentication);
        if (org == null) return notOrganization();
        return ResponseEntity.ok(b2bOrganizationService.profile(org.getOrgId()));
    }

    // [기관 정보 수정] PATCH /b2b/organization
    @PatchMapping
    public ResponseEntity<?> updateProfile(Authentication authentication,
                                           @RequestBody OrgProfileUpdateRequest request) {
        Organization org = organizationOf(authentication);
        if (org == null) return notOrganization();
        return ResponseEntity.ok(b2bOrganizationService.updateProfile(org.getOrgId(), request));
    }

    // [알림 설정 조회] GET /b2b/organization/notifications
    @GetMapping("/notifications")
    public ResponseEntity<?> notifications(Authentication authentication) {
        Organization org = organizationOf(authentication);
        if (org == null) return notOrganization();
        return ResponseEntity.ok(b2bOrganizationService.notifications(org.getOrgId()));
    }

    // [알림 설정 수정] PATCH /b2b/organization/notifications
    @PatchMapping("/notifications")
    public ResponseEntity<?> updateNotifications(
            Authentication authentication,
            @RequestBody NotificationSettingUpdateRequest request) {
        Organization org = organizationOf(authentication);
        if (org == null) return notOrganization();
        return ResponseEntity.ok(
                b2bOrganizationService.updateNotifications(org.getOrgId(), request));
    }

    // [API 키 목록] GET /b2b/organization/api-keys
    @GetMapping("/api-keys")
    public ResponseEntity<?> apiKeys(Authentication authentication) {
        Organization org = organizationOf(authentication);
        if (org == null) return notOrganization();
        return ResponseEntity.ok(b2bOrganizationService.apiKeys(org.getOrgId()));
    }

    // [API 키 발급] POST /b2b/organization/api-keys
    // 응답의 plainKey는 이때 한 번만 내려간다.
    @PostMapping("/api-keys")
    public ResponseEntity<?> issueApiKey(Authentication authentication,
                                         @RequestBody(required = false) ApiKeyCreateRequest request) {
        Organization org = organizationOf(authentication);
        if (org == null) return notOrganization();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(b2bOrganizationService.issueApiKey(org.getOrgId(), request));
    }

    // [API 키 폐기] DELETE /b2b/organization/api-keys/{keyId}
    @DeleteMapping("/api-keys/{keyId}")
    public ResponseEntity<?> revokeApiKey(Authentication authentication, @PathVariable Long keyId) {
        Organization org = organizationOf(authentication);
        if (org == null) return notOrganization();
        b2bOrganizationService.revokeApiKey(org.getOrgId(), keyId);
        return ResponseEntity.noContent().build();
    }

    private Organization organizationOf(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;
        Object principal = authentication.getPrincipal();
        return principal instanceof Organization org ? org : null;
    }

    private ResponseEntity<String> notOrganization() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("기관 계정으로 로그인해야 합니다.");
    }

    // 전역 예외 핸들러가 없어 잘못된 입력도 500으로 나간다.
    // 다른 컨트롤러 동작을 바꾸지 않도록 이 컨트롤러에만 붙인다.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
