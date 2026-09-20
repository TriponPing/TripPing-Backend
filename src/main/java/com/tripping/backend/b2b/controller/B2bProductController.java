package com.tripping.backend.b2b.controller;

import com.tripping.backend.b2b.dto.ProductCreateRequest;
import com.tripping.backend.b2b.dto.ProductUpdateRequest;
import com.tripping.backend.b2b.service.B2bProductService;
import com.tripping.backend.entity.Organization;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// 기관 전용 - 관광상품 기획.
//
// SecurityConfig의 anyRequest().authenticated()에 걸려 로그인은 이미 보장된다.
// 다만 운영자(Admin)도 로그인 상태이므로, 기관 계정인지는 여기서 확인한다.
@RequiredArgsConstructor
@RestController
@RequestMapping("/b2b/products")
public class B2bProductController {

    private final B2bProductService b2bProductService;

    // [상품 목록] GET /b2b/products
    @GetMapping
    public ResponseEntity<?> list(Authentication authentication) {
        Organization org = organizationOf(authentication);
        if (org == null) return notOrganization();
        return ResponseEntity.ok(b2bProductService.list(org.getOrgId()));
    }

    // [상품 상세] GET /b2b/products/{productId}
    @GetMapping("/{productId}")
    public ResponseEntity<?> detail(Authentication authentication, @PathVariable Long productId) {
        Organization org = organizationOf(authentication);
        if (org == null) return notOrganization();
        return ResponseEntity.ok(b2bProductService.detail(org.getOrgId(), productId));
    }

    // [새 상품 초안 생성] POST /b2b/products
    @PostMapping
    public ResponseEntity<?> create(Authentication authentication,
                                    @RequestBody(required = false) ProductCreateRequest request) {
        Organization org = organizationOf(authentication);
        if (org == null) return notOrganization();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(b2bProductService.create(org.getOrgId(), request));
    }

    // [상품 수정] PATCH /b2b/products/{productId}
    @PatchMapping("/{productId}")
    public ResponseEntity<?> update(Authentication authentication,
                                    @PathVariable Long productId,
                                    @RequestBody ProductUpdateRequest request) {
        Organization org = organizationOf(authentication);
        if (org == null) return notOrganization();
        return ResponseEntity.ok(b2bProductService.update(org.getOrgId(), productId, request));
    }

    // [상품 삭제] DELETE /b2b/products/{productId} - 소프트 삭제
    @DeleteMapping("/{productId}")
    public ResponseEntity<?> delete(Authentication authentication, @PathVariable Long productId) {
        Organization org = organizationOf(authentication);
        if (org == null) return notOrganization();
        b2bProductService.delete(org.getOrgId(), productId);
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

    // 이 프로젝트엔 전역 예외 핸들러가 없어 잘못된 입력도 500으로 나간다.
    // 다른 컨트롤러 동작을 바꾸지 않도록 이 컨트롤러에만 붙인다.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(java.util.NoSuchElementException.class)
    public ResponseEntity<String> handleNotFound(java.util.NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
