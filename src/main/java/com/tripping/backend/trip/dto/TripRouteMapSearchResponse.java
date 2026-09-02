package com.tripping.backend.trip.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class TripRouteMapSearchResponse {
    private Long routeId;
    private List<TripRouteMapPlaceResponse> places;

    public TripRouteMapSearchResponse(Long routeId, List<TripRouteMapPlaceResponse> places) {
        this.routeId = routeId;
        this.places = places;
    }
}