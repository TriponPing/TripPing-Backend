package com.tripping.backend.mypage.dto;

import java.util.List;

/**
 * 나의 여행 지도 조회 - GET /users/me/map
 */
public record MyMapResponse(
        List<MapPinResponse> pins,
        int visitedPlaceCount // 내가 다녀온 여행(저장한 루트 제외)에 포함된 장소 개수(중복 제거)
) {
}
