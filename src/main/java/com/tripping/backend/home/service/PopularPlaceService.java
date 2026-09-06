package com.tripping.backend.home.service;

import com.tripping.backend.entity.TouristSpot;
import com.tripping.backend.home.dto.response.PopularPlaceResponse;
import com.tripping.backend.home.repository.HomeSavedPlaceRepository;
import com.tripping.backend.home.repository.HomeTouristSpotRepository;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopularPlaceService {

    private static final int WEEK_WINDOW_DAYS = 7;

    private final HomeSavedPlaceRepository savedPlaceRepository;
    private final HomeTouristSpotRepository touristSpotRepository;

    /** 저장 수가 같은 값끼리는(동점) 순서 안에 같이 포함시키는 게 목적이라, 정렬만 하고 별도 중복 제거는 하지 않습니다. */
    public List<PopularPlaceResponse> getPopularPlaces(int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(WEEK_WINDOW_DAYS);
        Pageable topN = PageRequest.of(0, limit);

        List<Long> popularSpotIds = savedPlaceRepository.findPopularSpotIds(since, topN);
        if (popularSpotIds.isEmpty()) {
            return List.of();
        }

        // findAllById는 순서를 보장하지 않으므로 저장 수 내림차순(popularSpotIds 순서)대로 다시 정렬합니다.
        Map<Long, TouristSpot> spotById = new LinkedHashMap<>();
        touristSpotRepository.findAllById(popularSpotIds).forEach(spot -> spotById.put(spot.getSpotId(), spot));

        return popularSpotIds.stream()
                .map(spotById::get)
                .filter(spot -> spot != null)
                .map(spot -> PopularPlaceResponse.of(
                        spot,
                        savedPlaceRepository.countRecentSavesBySpotId(spot.getSpotId(), since)))
                .toList();
    }
}
