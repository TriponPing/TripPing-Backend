package com.tripping.backend.route.dto;

import com.tripping.backend.entity.PlannedRouteSpot;
import com.tripping.backend.entity.TouristSpot;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
public class RouteMapPlaceResponse {
    private Long routePlaceId;
    private Long spotId;
    private String spotName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer visitOrder;

    public RouteMapPlaceResponse(PlannedRouteSpot routeSpot, TouristSpot spot) {
        this.routePlaceId = routeSpot.getId();
        this.spotId = spot.getSpotId();
        this.spotName = spot.getName();
        this.latitude = spot.getLatitude();
        this.longitude = spot.getLongitude();
        this.visitOrder = routeSpot.getVisitOrder();
    }
}