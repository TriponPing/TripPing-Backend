package com.tripping.backend.place.service;

import com.tripping.backend.entity.SavedPlace;
import com.tripping.backend.place.dto.SavedPlaceResponse;
import com.tripping.backend.place.repository.SavedPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SavedPlaceService {

    private final SavedPlaceRepository savedPlaceRepository;

    // 북마크 저장
    @Transactional
    public SavedPlaceResponse savePlace(Long userId, Long spotId) {
        boolean alreadySaved = savedPlaceRepository.existsByUserIdAndSpotId(userId, spotId);

        if (!alreadySaved) {
            SavedPlace savedPlace = SavedPlace.builder()
                    .userId(userId)
                    .spotId(spotId)
                    .build();
            savedPlaceRepository.save(savedPlace);
        }

        return new SavedPlaceResponse(spotId, true);
    }

    // 북마크 삭제
    @Transactional
    public SavedPlaceResponse deletePlace(Long userId, Long spotId) {
        savedPlaceRepository.deleteByUserIdAndSpotId(userId, spotId);
        return new SavedPlaceResponse(spotId, false);
    }
}