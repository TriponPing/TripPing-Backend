package com.tripping.backend.b2b.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

// null인 항목은 "바꾸지 않음"으로 본다. 일정과 해시태그만 예외로,
// 빈 배열을 보내면 "전부 비움"이 된다(순서 전체를 통째로 교체하는 방식).
@Getter
@Setter
@NoArgsConstructor
public class ProductUpdateRequest {
    private String productName;
    private String status;
    private String regionId;
    private String targetCustomer;
    private Integer expectedDuration;
    private String transport;
    private Boolean mealIncluded;
    private String trendBasis;
    private String description;
    private Integer price;
    private String cautionNotes;
    private List<ProductSpotRequest> spots;
    private List<String> hashtags;
}
