package com.tripping.backend.home.controller;

import com.tripping.backend.auth.service.CustomUserDetails;
import com.tripping.backend.home.dto.response.CurrentTripSummaryResponse;
import com.tripping.backend.home.service.CurrentTripSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 홈 화면 "여행 중" 카드 전용. GET /routes/current(trip 도메인)와 별개로,
 * 카드에 필요한 Ping 개수/방문 장소까지 한 번에 묶어서 내려줌 - trip/ping 도메인 파일은 안 건드림.
 */
@Tag(name = "홈 - 진행 중 여행 요약")
@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
public class CurrentTripSummaryController {

    private final CurrentTripSummaryService currentTripSummaryService;

    @Operation(summary = "홈 화면용 진행 중 여행 요약 조회", description = "진행 중인 여행이 없으면 204 No Content를 반환합니다.")
    @GetMapping("/current-summary")
    public ResponseEntity<CurrentTripSummaryResponse> getCurrentTripSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        CurrentTripSummaryResponse response = currentTripSummaryService.getCurrentTripSummary(userDetails.getUserId());
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }
}
