package com.tripping.backend.place.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegisteredRouteCardResponse {
    private Long actualRouteId;
    private String themeName;   // "역사탐방 루트" 등 (카테고리 비율로 자동 결정)
    private String photoUrl;
    private int placeCount;
}