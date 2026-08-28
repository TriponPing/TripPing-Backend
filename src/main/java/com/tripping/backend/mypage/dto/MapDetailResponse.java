package com.tripping.backend.mypage.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 나의 여행 지도 상세 조회 (타입별 전체 경로)
 * GET /users/me/map/detail?type=drawn|saved
 */
public record MapDetailResponse(
        Long tripId,
        LocalDate travelDate,
        String type,
        List<SpotPoint> spots
) {
    public record SpotPoint(
            Integer visitOrder,
            Long spotId,
            String spotName,
            BigDecimal latitude,
            BigDecimal longitude,
            LocalDateTime visitTime
    ) {
    }
}
