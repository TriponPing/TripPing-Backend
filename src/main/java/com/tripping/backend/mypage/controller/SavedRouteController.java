package com.tripping.backend.mypage.controller;

import com.tripping.backend.auth.service.CustomUserDetails;
import com.tripping.backend.mypage.dto.SavedRouteSaveResponse;
import com.tripping.backend.mypage.service.MyPageRouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * 마이페이지 소유가 아닌 /routes 하위 경로라서 컨트롤러를 분리했습니다.
 */
@Tag(name = "마이페이지", description = "프로필, 다녀온 여행, 저장한 루트, 나의 여행 지도 API")
@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
public class SavedRouteController {

    private final MyPageRouteService routeService;

    @Operation(summary = "루트 저장(북마크)", description = "다른 사람이 공개해둔 다녀온 여행(루트)을 저장(북마크)합니다. 이미 저장돼있으면 그대로 saved=true만 반환합니다.")
    @PostMapping("/{routeId}/saved")
    public ResponseEntity<SavedRouteSaveResponse> saveRoute(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long routeId
    ) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return ResponseEntity.ok(routeService.saveRoute(userDetails.getUserId(), routeId));
    }

    @Operation(summary = "루트 저장 취소(북마크 해제)", description = "저장(북마크)해둔 루트를 취소합니다.")
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
