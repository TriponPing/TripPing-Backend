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

    // 👈 새로 추가: 이 장소 후기 중 저장 많이 된 루트의 사진을 우선으로 골라서 내려줌.
    // 사진 등록된 후기가 하나도 없으면 null (프론트는 이때 자리별 고정 이미지로 대체함).
    @Schema(description = "저장 수 많은 루트의 이 장소 후기 사진 (없으면 null)")
    private String photoUrl;

    public static PopularPlaceResponse of(TouristSpot spot, long savedCount, String photoUrl) {
        return PopularPlaceResponse.builder()
                .spotId(spot.getSpotId())
                .name(spot.getName())
                .category(spot.getCategory())
                .address(spot.getAddress())
                .savedCount(savedCount)
                .photoUrl(photoUrl)
                .build();
    }
}
