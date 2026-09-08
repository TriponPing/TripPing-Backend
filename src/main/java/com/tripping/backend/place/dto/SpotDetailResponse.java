package com.tripping.backend.place.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SpotDetailResponse {
    private Long spotId;
    private String name;
    private String address;
    private String category;
    private String imageUrl;
    private String description;
    private long pingCount;
    private String popularTimeSlot;
    private List<RegisteredRouteCardResponse> registeredRoutes;
    private List<SpotReviewResponse> reviews;
}