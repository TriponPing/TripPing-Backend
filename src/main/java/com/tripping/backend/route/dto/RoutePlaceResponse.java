package com.tripping.backend.route.dto;

import com.tripping.backend.entity.PlannedRouteSpot;
import lombok.Getter;

@Getter
public class RoutePlaceResponse {
    private Long routePlaceId;
    private Long spotId;
    private String spotName;
    private Integer visitOrder;

    public RoutePlaceResponse(PlannedRouteSpot spot, String spotName) {
        this.routePlaceId = spot.getId();
        this.spotId = spot.getSpotId();
        this.spotName = spotName;
        this.visitOrder = spot.getVisitOrder();
    }
}