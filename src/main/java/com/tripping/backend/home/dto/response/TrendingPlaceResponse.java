package com.tripping.backend.home.dto.response;

import com.tripping.backend.entity.TouristSpot;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "떠오르는 인기 장소 응답")
public class TrendingPlaceResponse {

    @Schema(description = "관광지 ID", example = "1")
    private Long spotId;

    @Schema(description = "관광지 이름", example = "해운대해수욕장")
    private String name;

    @Schema(description = "카테고리", example = "해변")
    private String category;

    @Schema(description = "주소", example = "부산 해운대구 우동")
    private String address;

    @Schema(description = "이미지 URL")
    private String imageUrl;

    @Schema(description = "위도")
    private BigDecimal latitude;

    @Schema(description = "경도")
    private BigDecimal longitude;

    @Schema(description = "최근 방문(핑) 횟수", example = "42")
    private Long recentVisitCount;

    public static TrendingPlaceResponse from(TouristSpot spot, long recentVisitCount) {
        return TrendingPlaceResponse.builder()
                .spotId(spot.getSpotId())
                .name(spot.getName())
                .category(spot.getCategory())
                .address(spot.getAddress())
                .imageUrl(spot.getImageUrl())
                .latitude(spot.getLatitude())
                .longitude(spot.getLongitude())
                .recentVisitCount(recentVisitCount)
                .build();
    }
}
