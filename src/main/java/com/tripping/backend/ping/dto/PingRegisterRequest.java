package com.tripping.backend.ping.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 방문 장소 Ping 등록 요청
 * POST /routes/{routeId}/pings
 */
public record PingRegisterRequest(

        Long spotId, // 검색으로 매칭된 관광지가 있으면 그 id, 직접 입력한 장소면 null

        @NotBlank(message = "장소 이름을 입력해주세요.")
        String placeName,

        @NotNull(message = "위도는 필수입니다.")
        BigDecimal latitude,

        @NotNull(message = "경도는 필수입니다.")
        BigDecimal longitude
) {
}
