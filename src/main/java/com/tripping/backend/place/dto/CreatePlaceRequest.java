package com.tripping.backend.place.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 새 장소 등록 요청
 * POST /places
 */
public record CreatePlaceRequest(

        @NotBlank(message = "장소 이름을 입력해주세요.")
        String name,

        @NotBlank(message = "카테고리를 입력해주세요.")
        String category,

        @NotNull(message = "위도는 필수입니다.")
        BigDecimal latitude,

        @NotNull(message = "경도는 필수입니다.")
        BigDecimal longitude
) {
}