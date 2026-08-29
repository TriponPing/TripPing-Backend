package com.tripping.backend.route.dto;

import lombok.Getter;

@Getter
public class RouteCreateRequest {
    private String title;
    private Long candidateId; // 추천 기반이면 값 있음, 직접 생성이면 null
}