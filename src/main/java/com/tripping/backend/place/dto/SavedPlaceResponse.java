package com.tripping.backend.place.dto;

import lombok.Getter;

@Getter
public class SavedPlaceResponse {
    private Long spotId;
    private boolean saved;

    public SavedPlaceResponse(Long spotId, boolean saved) {
        this.spotId = spotId;
        this.saved = saved;
    }
}