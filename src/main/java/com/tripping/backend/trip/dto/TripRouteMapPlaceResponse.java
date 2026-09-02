package com.tripping.backend.trip.dto;

import com.tripping.backend.trip.repository.TripActualRouteSpotRepository.TripRouteMapSpotProjection;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class TripRouteMapPlaceResponse {
    private Long routePlaceId;
    private Long spotId;
    private String spotName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer visitOrder;

    public TripRouteMapPlaceResponse(TripRouteMapSpotProjection p) {
        this.routePlaceId = p.getRoutePlaceId();
        this.spotId = p.getSpotId();
        this.spotName = p.getSpotName();
        this.latitude = p.getLatitude();
        this.longitude = p.getLongitude();
        this.visitOrder = p.getVisitOrder();
    }
}