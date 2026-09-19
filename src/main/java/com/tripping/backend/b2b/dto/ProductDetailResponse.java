package com.tripping.backend.b2b.dto;

import com.tripping.backend.entity.TourProduct;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

// tour_product에 가격 컬럼이 없어 판매가는 내려가지 않는다. 회의록은 가격을
// 요구하므로 컬럼 추가 여부가 정해지면 여기에 같이 싣는다.
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
                .cautionNotes(product.getCautionNotes())
                .spots(spots)
                .hashtags(hashtags)
                .updatedAt(product.getUpdatedAt())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
