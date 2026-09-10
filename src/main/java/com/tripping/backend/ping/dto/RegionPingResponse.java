package com.tripping.backend.ping.dto;

import com.tripping.backend.entity.RegionPing;
import com.tripping.backend.entity.TouristSpot;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 지역핑 등록 응답 - 등록 직후 프론트에서 바로 보여줄 수 있게 장소 정보까지 함께 내려줌
public record RegionPingResponse(
        Long regionPingId,
        Long spotId,
        String spotName,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        String regionId,
        Integer rating,
        String reviewComment,
        String writerNickname,
        LocalDateTime createdAt
) {
    public static RegionPingResponse of(RegionPing regionPing, TouristSpot spot, String writerNickname) {
        return new RegionPingResponse(
                regionPing.getRegionPingId(),
                regionPing.getSpotId(),
                spot != null ? spot.getName() : null,
                spot != null ? spot.getAddress() : null,
                spot != null ? spot.getLatitude() : null,
                spot != null ? spot.getLongitude() : null,
                spot != null ? spot.getRegionId() : null,
                regionPing.getRating(),
                regionPing.getReviewComment(),
                writerNickname,
                regionPing.getCreatedAt()
        );
    }
}
