package com.tripping.backend.place.service;

import com.tripping.backend.entity.TouristSpot;
import com.tripping.backend.ping.dto.SpotPingStatsResponse;
import com.tripping.backend.ping.service.PingService;
import com.tripping.backend.place.dto.TouristSpotResponse;
import com.tripping.backend.place.repository.PlaceTouristSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TouristSpotService {

    private final PlaceTouristSpotRepository touristSpotRepository;
    private final PingService pingService;

    public List<TouristSpotResponse> searchByLocation(double lat, double lng, double radius) {
        return touristSpotRepository.findWithinRadius(lat, lng, radius).stream()
                .map(TouristSpotResponse::new)
                .toList();
    }

    public List<TouristSpotResponse> getByCategory(String category) {
        return touristSpotRepository.findByCategory(category).stream()
                .map(TouristSpotResponse::new)
                .toList();
    }

    // 탐색 지도 필터 기능 (지역, 시간대, 핑개수 조건 지원)
    public List<TouristSpotResponse> getByCategoryAndFilters(
            String category,
            String regionId,
            String timeSlot,
            Integer minPingCount
    ) {
        List<TouristSpot> spots;

        if (regionId != null && !regionId.isBlank()) {
            spots = touristSpotRepository.findByCategoryAndRegionId(category, regionId);
        } else {
            spots = touristSpotRepository.findByCategory(category);
        }

        if (timeSlot != null || minPingCount != null) {
            spots = spots.stream()
                    .filter(spot -> matchesPingFilter(spot.getSpotId(), timeSlot, minPingCount))
                    .toList();
        }

        return spots.stream()
                .map(TouristSpotResponse::new)
                .toList();
    }

    private boolean matchesPingFilter(Long spotId, String timeSlot, Integer minPingCount) {
        SpotPingStatsResponse stats = pingService.getSpotPingStats(spotId);

        if (timeSlot != null && !timeSlot.equals(stats.popularTimeSlot())) {
            return false;
        }

        if (minPingCount != null && stats.totalPingCount() < minPingCount) {
            return false;
        }

        return true;
    }

    public TouristSpotResponse getDetail(Long placeId) {
        return touristSpotRepository.findById(placeId)
                .map(TouristSpotResponse::new)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장소입니다. id=" + placeId));
    }
}