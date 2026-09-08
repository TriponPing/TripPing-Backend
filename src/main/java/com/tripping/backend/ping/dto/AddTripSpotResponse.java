package com.tripping.backend.ping.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 완료된 여행에 방문 스팟 추가 응답
 * POST /trips/{routeId}/spots
 */
public record AddTripSpotResponse(
        Integer visitOrder,
        Long spotId,
        String spotName,
        String category,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDateTime visitTime
) {
}