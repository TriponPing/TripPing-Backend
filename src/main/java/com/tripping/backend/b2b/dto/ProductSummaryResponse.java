package com.tripping.backend.b2b.dto;

import com.tripping.backend.entity.TourProduct;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProductSummaryResponse {
    private Long productId;
    private String productName;
    private String status;
    private String regionId;
    private String regionName;
    private int spotCount;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;

    public static ProductSummaryResponse of(TourProduct product, String regionName, int spotCount) {
        return ProductSummaryResponse.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .status(product.getStatus())
                .regionId(product.getRegionId())
                .regionName(regionName)
                .spotCount(spotCount)
                .updatedAt(product.getUpdatedAt())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
