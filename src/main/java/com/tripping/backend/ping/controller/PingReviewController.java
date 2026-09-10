package com.tripping.backend.ping.controller;

import com.tripping.backend.auth.service.CustomUserDetails;
import com.tripping.backend.ping.dto.PingReviewRequest;
import com.tripping.backend.ping.dto.PingReviewResponse;
import com.tripping.backend.ping.service.PingReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Ping", description = "여행 중 방문 장소 Ping 등록/조회 API")
@RestController
@RequestMapping("/pings")
@RequiredArgsConstructor
public class PingReviewController {

    private final PingReviewService pingReviewService;

    // 👈 새로 추가: GET 매핑이 빠져있어서 프론트가 "기존 후기 있는지" 확인할 때 405가 나던 문제 수정.
    // 이것 때문에 항상 "등록" 모드로만 뜨고, 이미 있는 후기가 있어도 수정 모드로 전환이 안 되고 있었음.
    @Operation(summary = "Ping 후기 조회", description = "등록된 후기를 조회합니다. 없으면 404 - 후기 작성 화면 진입 시 등록/수정 모드 판단용.")
    @GetMapping("/{pingId}/review")
    public ResponseEntity<PingReviewResponse> getReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long pingId
    ) {
        requireLogin(userDetails);
        return ResponseEntity.ok(pingReviewService.getReview(userDetails.getUserId(), pingId));
    }

    @Operation(summary = "Ping 후기 등록", description = "방문 스팟(pingId=ACTUAL_ROUTE_SPOT id)에 평점/사진/코멘트 후기를 등록합니다.")
    @PostMapping("/{pingId}/review")
    public ResponseEntity<PingReviewResponse> createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long pingId,
            @Valid @RequestBody PingReviewRequest request
    ) {
        requireLogin(userDetails);
        PingReviewResponse response = pingReviewService.createReview(userDetails.getUserId(), pingId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Ping 후기 수정", description = "등록된 후기를 부분 수정합니다(null 필드는 그대로 둠).")
    @PatchMapping("/{pingId}/review")
    public ResponseEntity<PingReviewResponse> updateReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long pingId,
            @Valid @RequestBody PingReviewRequest request
    ) {
        requireLogin(userDetails);
        return ResponseEntity.ok(pingReviewService.updateReview(userDetails.getUserId(), pingId, request));
    }

    @Operation(summary = "Ping 후기 삭제", description = "등록된 후기를 소프트 삭제합니다.")
    @DeleteMapping("/{pingId}/review")
    public ResponseEntity<Void> deleteReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long pingId
    ) {
        requireLogin(userDetails);
        pingReviewService.deleteReview(userDetails.getUserId(), pingId);
        return ResponseEntity.noContent().build();
    }

    private void requireLogin(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
    }
}
