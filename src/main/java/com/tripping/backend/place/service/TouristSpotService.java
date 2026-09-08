package com.tripping.backend.place.service;

import com.tripping.backend.entity.TouristSpot;
import com.tripping.backend.ping.dto.SpotPingStatsResponse;
import com.tripping.backend.ping.service.PingService;
import com.tripping.backend.place.dto.CreatePlaceRequest;
import com.tripping.backend.place.dto.TouristSpotResponse;
import com.tripping.backend.place.repository.PlaceTouristSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    public List<TouristSpotResponse> searchByKeyword(String query, String regionId) {
        List<TouristSpot> spots = (regionId != null && !regionId.isBlank())
                ? touristSpotRepository.findByRegionIdAndNameContainingIgnoreCase(regionId, query)
                : touristSpotRepository.findByNameContainingIgnoreCase(query);

        return spots.stream()
                .map(spot -> {
                    SpotPingStatsResponse stats = pingService.getSpotPingStats(spot.getSpotId());
                    return new TouristSpotResponse(spot, stats.popularTimeSlot(), stats.totalPingCount());
                })
                .toList();
    }

    // 👈 새로 추가: 새 장소 등록 - POST /places
    // 네이버맵 POI 등 아직 우리 DB에 없는 장소를 클라이언트가 새로 등록할 때 씀
    // ⚠️ TouristSpot.builder() 구성은 SavedPlace 엔티티의 빌더 패턴을 보고 추측했습니다.
    // 실제 TouristSpot 엔티티 파일 보여주시면 정확히 맞춰드릴게요.
    @Transactional
    public TouristSpotResponse createPlace(CreatePlaceRequest request) {
        TouristSpot spot = TouristSpot.builder()
                .name(request.name())
                .category(request.category())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .build();

        TouristSpot saved = touristSpotRepository.save(spot);
        return new TouristSpotResponse(saved);
    }
}