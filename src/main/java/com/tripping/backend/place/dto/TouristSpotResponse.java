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

    public TouristSpotResponse(TouristSpot spot) {
        this.spotId = spot.getSpotId();
        this.name = spot.getName();
        this.category = spot.getCategory();
        this.address = spot.getAddress();
        this.latitude = spot.getLatitude();
        this.longitude = spot.getLongitude();
        this.imageUrl = spot.getImageUrl();
        this.description = spot.getDescription();
    }
}