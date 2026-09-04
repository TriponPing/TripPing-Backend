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
        String representativeImageUrl,
        Integer placeCount // 이 여행에 포함된 방문 장소(스팟) 개수 - 프론트 카드의 "핑 N개" 표시에 사용
) {
}
