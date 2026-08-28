package com.tripping.backend.mypage.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 나의 여행 지도 조회 (지도에 찍을 핀 1개 = 여행 1개, 대표 좌표만)
 * GET /users/me/map
 */
public record MapPinResponse(
        Long tripId,
        String type,               // DRAWN(다녀온 여행) | SAVED(저장한 루트)
        LocalDate travelDate,
        BigDecimal latitude,
        BigDecimal longitude,
        String representativeSpotName
) {
}
