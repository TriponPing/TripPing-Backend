package com.tripping.backend.b2b.dto;

import com.tripping.backend.entity.TourProduct;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ProductDetailResponse {
    private Long productId;
    private String productName;
    private String status;
    private String regionId;
    private String regionName;
    private String targetCustomer;
    private Integer expectedDuration; // 분 단위
    private String transport;
    private Boolean mealIncluded;
    private String trendBasis;
    private String description;
    private Integer price;
    private String cautionNotes;
    private List<ProductSpotResponse> spots;
    private List<String> hashtags;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;

    public static ProductDetailResponse of(TourProduct product, String regionName,
                                           List<ProductSpotResponse> spots, List<String> hashtags) {
        return ProductDetailResponse.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .status(product.getStatus())
                .regionId(product.getRegionId())
                .regionName(regionName)
                .targetCustomer(product.getTargetCustomer())
                .expectedDuration(product.getExpectedDuration())
                .transport(product.getTransport())
                .mealIncluded(product.getMealIncluded())
                .trendBasis(product.getTrendBasis())
                .description(product.getDescription())
                .price(product.getPrice())
                .cautionNotes(product.getCautionNotes())
                .spots(spots)
                .hashtags(hashtags)
                .updatedAt(product.getUpdatedAt())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
