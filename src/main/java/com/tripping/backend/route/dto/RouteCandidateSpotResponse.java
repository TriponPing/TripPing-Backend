package com.tripping.backend.route.dto;

import com.tripping.backend.entity.TouristSpot;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
public class RouteCandidateSpotResponse {
    private Long spotId;
    private String name;
    private String category;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer visitOrder;

    public RouteCandidateSpotResponse(TouristSpot spot, int visitOrder) {
        this.spotId = spot.getSpotId();
        this.name = spot.getName();
        this.category = spot.getCategory();
        this.latitude = spot.getLatitude();
        this.longitude = spot.getLongitude();
        this.visitOrder = visitOrder;
    }
}