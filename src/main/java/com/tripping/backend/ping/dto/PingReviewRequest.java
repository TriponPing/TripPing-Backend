package com.tripping.backend.ping.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Ping 후기 등록/수정 요청
 * POST/PATCH /pings/{pingId}/review
 * (수정은 partial update - null 필드는 그대로 둠. 등록 시 rating은 서비스단에서 필수 체크)
 */
public record PingReviewRequest(

        @Min(value = 1, message = "평점은 1~5 사이여야 합니다.")
        @Max(value = 5, message = "평점은 1~5 사이여야 합니다.")
        Integer rating,

        String photoUrl,

        @Size(max = 2000, message = "후기는 2000자를 넘을 수 없습니다.")
        String reviewComment
) {
}
