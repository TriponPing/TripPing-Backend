package com.tripping.backend.ping.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 지역핑 등록 요청
 * POST /region-pings
 * spotId: 등록할 장소. 이미 DB에 있는 장소면 그 spotId, 새 장소면 먼저 POST /places로
 * 등록한 뒤 그 응답의 spotId를 넣어서 보냄 (기존 "새 장소 등록" 흐름 재사용).
 */
public record RegionPingRequest(

        @NotNull(message = "장소를 선택해주세요.")
        Long spotId,

        @NotNull(message = "평점을 입력해주세요.")
        @Min(value = 1, message = "평점은 1~5 사이여야 합니다.")
        @Max(value = 5, message = "평점은 1~5 사이여야 합니다.")
        Integer rating,

        @Size(max = 500, message = "후기는 500자를 넘을 수 없습니다.")
        String reviewComment
) {
}
