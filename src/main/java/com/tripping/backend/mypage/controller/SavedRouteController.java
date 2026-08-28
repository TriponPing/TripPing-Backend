package com.tripping.backend.mypage.controller;

import com.tripping.backend.auth.service.CustomUserDetails;
import com.tripping.backend.mypage.service.MyPageRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * 마이페이지 소유가 아닌 /routes 하위 경로라서 컨트롤러를 분리했습니다.
 */
@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
public class SavedRouteController {

    private final MyPageRouteService routeService;

    // 루트 저장 취소 (북마크 해제)
    @DeleteMapping("/{routeId}/saved")
    public ResponseEntity<Void> unsaveRoute(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long routeId
    ) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        routeService.unsaveRoute(userDetails.getUserId(), routeId);
        return ResponseEntity.noContent().build();
    }
}
