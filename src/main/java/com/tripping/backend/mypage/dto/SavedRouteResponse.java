package com.tripping.backend.mypage.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 저장한 루트 목록 조회
 * GET /users/me/routes/saved
 */
public record SavedRouteResponse(
        Long savedRouteId,
        Long tripId,               // 참조하는 ACTUAL_ROUTE의 id (북마크 해제 시 이 값 사용)
        LocalDate travelDate,
        Integer memberCount,
        String representativeSpotName,
        String representativeImageUrl,
        LocalDateTime savedAt
) {
}
