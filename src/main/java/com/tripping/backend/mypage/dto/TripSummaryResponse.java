package com.tripping.backend.mypage.dto;

import java.time.LocalDate;

/**
 * 다녀온 여행 목록 (요약 / 전체 공용)
 * GET /users/me/trips/recent, GET /users/me/trips
 */
public record TripSummaryResponse(
        Long tripId,
        LocalDate travelDate,
        String status,
        Integer memberCount,
        String representativeSpotName,
        String representativeImageUrl
) {
}
