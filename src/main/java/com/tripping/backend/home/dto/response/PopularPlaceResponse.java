package com.tripping.backend.home.dto.response;

import com.tripping.backend.entity.TouristSpot;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "이번주 인기 장소 응답 (저장 수 기준)")
public class PopularPlaceResponse {

    @Schema(description = "장소(관광지) ID")
    private Long spotId;

    @Schema(description = "장소 이름", example = "서울 암사동 유적")
    private String name;

    @Schema(description = "카테고리", example = "관광지")
    private String category;

    @Schema(description = "주소", example = "서울 종로구 사직로 161")
    private String address;

    @Schema(description = "이번 주 저장(찜) 수", example = "33")
    private long savedCount;

    public static PopularPlaceResponse of(TouristSpot spot, long savedCount) {
        return PopularPlaceResponse.builder()
                .spotId(spot.getSpotId())
                .name(spot.getName())
                .category(spot.getCategory())
                .address(spot.getAddress())
                .savedCount(savedCount)
                .build();
    }
}
