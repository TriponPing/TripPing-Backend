package com.tripping.backend.home.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StopSummaryResponse {
    private Long spotId;
    private String name;
    private Double averageRating;      // 평점 없으면 0.0 (COALESCE 처리됨)
    private long registeredRouteCount; // 이 장소가 포함된 루트 개수
}