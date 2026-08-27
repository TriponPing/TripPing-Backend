package com.tripping.backend.ping.controller;

import com.tripping.backend.auth.service.CustomUserDetails;
import com.tripping.backend.ping.dto.PingReviewRequest;
import com.tripping.backend.ping.dto.PingReviewResponse;
import com.tripping.backend.ping.service.PingReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/pings")
@RequiredArgsConstructor
public class PingReviewController {

    private final PingReviewService pingReviewService;

    // Ping 후기 등록
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

    // Ping 후기 수정
    @PatchMapping("/{pingId}/review")
    public ResponseEntity<PingReviewResponse> updateReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long pingId,
            @Valid @RequestBody PingReviewRequest request
    ) {
        requireLogin(userDetails);
        return ResponseEntity.ok(pingReviewService.updateReview(userDetails.getUserId(), pingId, request));
    }

    // Ping 후기 삭제
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
