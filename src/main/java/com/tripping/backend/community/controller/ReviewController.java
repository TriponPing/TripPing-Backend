package com.tripping.backend.community.controller;

import com.tripping.backend.auth.service.CustomUserDetails;
import com.tripping.backend.community.dto.request.ReviewUpdateRequest;
import com.tripping.backend.community.dto.response.ReviewResponse;
import com.tripping.backend.community.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "커뮤니티 - 후기")
@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "루트 후기 조회", description = "특정 루트에 달린 후기 목록을 조회합니다. 로그인 없이도 조회 가능합니다.")
    @GetMapping("/routes/{routeId}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getReviews(
            @PathVariable Long routeId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(reviewService.getReviewsByRoute(routeId, pageable));
    }

    @Operation(summary = "내 후기 수정", description = "본인이 작성한 후기만 수정할 수 있습니다.")
    @PatchMapping("/reviews/me/{reviewId}")
    public ResponseEntity<?> updateMyReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 필요");
        }

        ReviewResponse response = reviewService.updateMyReview(userDetails.getUserId(), reviewId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 후기 삭제", description = "본인이 작성한 후기만 삭제할 수 있습니다.")
    @DeleteMapping("/reviews/me/{reviewId}")
    public ResponseEntity<?> deleteMyReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reviewId
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 필요");
        }

        reviewService.deleteMyReview(userDetails.getUserId(), reviewId);
        return ResponseEntity.noContent().build();
    }
}
