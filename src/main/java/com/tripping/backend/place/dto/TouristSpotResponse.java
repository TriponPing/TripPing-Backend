package com.tripping.backend.place.dto;

import com.tripping.backend.entity.TouristSpot;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
public class TouristSpotResponse {
    private Long spotId;
    private String name;
    private String category;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String imageUrl;
    private String description;
    private String popularTimeSlot; // 가장 많이 가는 시간대
    private long pingCount;         // 핑 개수

    public TouristSpotResponse(TouristSpot spot) {
        this(spot, null, 0L);
    }

    public TouristSpotResponse(TouristSpot spot, String popularTimeSlot, long pingCount) {
        this.spotId = spot.getSpotId();
        this.name = spot.getName();
        this.category = spot.getCategory();
        this.address = spot.getAddress();
        this.latitude = spot.getLatitude();
        this.longitude = spot.getLongitude();
        this.imageUrl = spot.getImageUrl();
        this.description = spot.getDescription();
        this.popularTimeSlot = popularTimeSlot;
        this.pingCount = pingCount;
    }
}