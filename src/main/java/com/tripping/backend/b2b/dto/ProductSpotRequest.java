package com.tripping.backend.b2b.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductSpotRequest {
    private Long spotId;
    private Integer stayDuration; // 분 단위, 없으면 비워둔다
}
