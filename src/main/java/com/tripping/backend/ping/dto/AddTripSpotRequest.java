package com.tripping.backend.ping.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 완료된 여행에 놓친 방문 스팟을 나중에 추가하는 요청
 * POST /trips/{routeId}/spots
 */
public record AddTripSpotRequest(
        @NotNull(message = "spotId는 필수입니다.")
        Long spotId,

        @NotNull(message = "위도는 필수입니다.")
        BigDecimal latitude,

        @NotNull(message = "경도는 필수입니다.")
        BigDecimal longitude
) {
}