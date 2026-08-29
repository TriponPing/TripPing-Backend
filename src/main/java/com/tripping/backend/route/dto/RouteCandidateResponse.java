package com.tripping.backend.route.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class RouteCandidateResponse {
    private int candidateOrder;
    private String theme;
    private double totalDistanceKm;
    private int totalWalkTimeMinutes;
    private List<RouteCandidateSpotResponse> spots;

    public RouteCandidateResponse(int candidateOrder, String theme, double totalDistanceKm,
                                  int totalWalkTimeMinutes, List<RouteCandidateSpotResponse> spots) {
        this.candidateOrder = candidateOrder;
        this.theme = theme;
        this.totalDistanceKm = totalDistanceKm;
        this.totalWalkTimeMinutes = totalWalkTimeMinutes;
        this.spots = spots;
    }
}