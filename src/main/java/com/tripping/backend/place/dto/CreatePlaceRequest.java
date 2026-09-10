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
        BigDecimal longitude,

        // 👈 새로 추가: 지역핑 등록 흐름에서 새 장소를 만들 때, 그 장소가 어느 지역 소속인지 같이 넘겨줌
        // (지역핑은 본인 거주 지역의 장소에만 등록 가능해서, 이게 없으면 새로 만든 장소는 영원히 어느 지역과도 매칭이 안 됨)
        // 선택값 - 기존 호출부(트립 중 장소 검색 등)는 안 보내도 그대로 동작함
        String regionId
) {
}