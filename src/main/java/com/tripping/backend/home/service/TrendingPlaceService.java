package com.tripping.backend.home.service;

import com.tripping.backend.entity.TouristSpot;
import com.tripping.backend.home.dto.response.TrendingPlaceResponse;
import com.tripping.backend.home.repository.HomeActualRouteSpotRepository;
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
public class TrendingPlaceService {

    /** 최근 며칠 안의 방문 기록을 "트렌딩" 집계 대상으로 볼지. 필요하면 값만 조정하세요. */
    private static final int TRENDING_WINDOW_DAYS = 14;

    private final HomeActualRouteSpotRepository actualRouteSpotRepository;
    private final HomeTouristSpotRepository touristSpotRepository;

    public List<TrendingPlaceResponse> getTrendingPlaces(int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(TRENDING_WINDOW_DAYS);
        Pageable topN = PageRequest.of(0, limit);

        List<Long> trendingSpotIds = actualRouteSpotRepository.findTrendingSpotIds(since, topN);
        if (trendingSpotIds.isEmpty()) {
            return List.of();
        }

        // findAllById 는 순서를 보장하지 않으므로, spotId -> TouristSpot 맵으로 만들고
        // trendingSpotIds 순서(방문 횟수 내림차순)대로 다시 정렬합니다.
        Map<Long, TouristSpot> spotById = new LinkedHashMap<>();
        touristSpotRepository.findAllById(trendingSpotIds)
                .forEach(spot -> spotById.put(spot.getSpotId(), spot));

        return trendingSpotIds.stream()
                .map(spotById::get)
                .filter(spot -> spot != null)
                .map(spot -> TrendingPlaceResponse.from(
                        spot,
                        actualRouteSpotRepository.countRecentVisitsBySpotId(spot.getSpotId(), since)))
                .toList();
    }
}
