package com.tripping.backend.b2b.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductSpotResponse {
    private Long spotId;
    private String name;
    private String address;
    private java.math.BigDecimal latitude;
    private java.math.BigDecimal longitude;
    private Integer visitOrder;
    private Integer stayDuration; // 분 단위
}
