package com.tripping.backend.b2b.dto;

import com.tripping.backend.entity.TouristSpot;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

// 관광지 검색 결과 한 건.
//
// registered=true면 이미 우리 DB에 있어 spotId로 바로 일정에 담을 수 있고,
// false면 관광공사 후보라 담기 전에 contentId로 등록해야 한다.
@Getter
@Builder
public class SpotSearchResponse {
    private Long spotId;
    private String contentId;
    private String name;
    private String address;
    private String category;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String imageUrl;
    private boolean registered;

    public static SpotSearchResponse saved(TouristSpot spot) {
        return SpotSearchResponse.builder()
                .spotId(spot.getSpotId())
                .contentId(spot.getApiContentId())
                .name(spot.getName())
                .address(spot.getAddress())
                .category(spot.getCategory())
                .latitude(spot.getLatitude())
                .longitude(spot.getLongitude())
                .imageUrl(spot.getImageUrl())
                .registered(true)
                .build();
    }
}
