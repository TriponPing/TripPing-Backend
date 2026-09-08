package com.tripping.backend.place.controller;

import com.tripping.backend.auth.service.CustomUserDetails;
import com.tripping.backend.place.dto.SavedPlaceResponse;
import com.tripping.backend.place.service.SavedPlaceService;
import com.tripping.backend.place.dto.TouristSpotResponse;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/places")
@RequiredArgsConstructor
@Tag(name = "탐색", description = "장소 북마크 API")
public class SavedPlaceController {

    private final SavedPlaceService savedPlaceService;

    @PostMapping("/{placeId}/saved/me")
    @Operation(summary = "장소 북마크 저장")
    public ResponseEntity<SavedPlaceResponse> savePlace(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long placeId
    ) {
        return ResponseEntity.ok(savedPlaceService.savePlace(userDetails.getUserId(), placeId));
    }

    @GetMapping("/saved/me")
    @Operation(summary = "내가 저장한 장소 목록 조회")
    public ResponseEntity<List<TouristSpotResponse>> getSavedPlaces(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(savedPlaceService.getSavedPlaces(userDetails.getUserId()));
    }

    @DeleteMapping("/{placeId}/saved/me")
    @Operation(summary = "장소 북마크 삭제")
    public ResponseEntity<SavedPlaceResponse> deletePlace(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long placeId
    ) {
        return ResponseEntity.ok(savedPlaceService.deletePlace(userDetails.getUserId(), placeId));
    }
}