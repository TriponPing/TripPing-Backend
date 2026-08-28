package com.tripping.backend.mypage.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 여행 기록 상세 조회
 * GET /users/me/trips/{tripId}
 */
public record TripDetailResponse(
        Long tripId,
        LocalDate travelDate,
        String companionType,
        String transport,
        Integer memberCount,
        String status,
        Boolean isPublic,
        List<SpotDetail> spots
) {
    public record SpotDetail(
            Integer visitOrder,
            Long spotId,
            String spotName,
            String category,
            String address,
            BigDecimal latitude,
            BigDecimal longitude,
            LocalDateTime visitTime,
            Integer rating,
            String photoUrl,
            String reviewComment
    ) {
    }
}
