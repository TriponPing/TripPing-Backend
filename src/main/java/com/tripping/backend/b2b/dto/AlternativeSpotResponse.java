package com.tripping.backend.b2b.dto;

import com.tripping.backend.b2b.repository.B2bAlternativeSpotRepository.AlternativeSpotRow;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

// 대체 관광지 후보 한 건.
//
// visitCount와 averageRating은 추천 순서의 근거라 화면에도 같이 내려준다.
// 핑이 쌓이기 전에는 visitCount=0, averageRating=null로 내려가고, 이때는
// distanceKm만으로 순서가 정해진다.
@Getter
@Builder
public class AlternativeSpotResponse {
    private Long spotId;
    private String name;
    private String category;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String imageUrl;
    private long visitCount;
    private Double averageRating;
    private double distanceKm;

    public static AlternativeSpotResponse from(AlternativeSpotRow row) {
        return AlternativeSpotResponse.builder()
                .spotId(row.getSpotId())
                .name(row.getName())
                .category(row.getCategory())
                .address(row.getAddress())
                .latitude(row.getLatitude())
                .longitude(row.getLongitude())
                .imageUrl(row.getImageUrl())
                .visitCount(row.getVisitCount())
                .averageRating(row.getAverageRating())
                // 화면에 "1.2km"로 보여주므로 소수점 한 자리까지만 내려준다.
                .distanceKm(Math.round(row.getDistanceKm() * 10) / 10.0)
                .build();
    }
}
